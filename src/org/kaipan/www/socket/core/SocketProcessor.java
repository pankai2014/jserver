package org.kaipan.www.socket.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.kaipan.www.socket.log.Logger;
import org.kaipan.www.socket.router.IRouter;
import org.kaipan.www.socket.ssl.Ssl;
import org.kaipan.www.socket.ssl.SslConfig;
import org.kaipan.www.socket.ssl.SslEngine;
import org.kaipan.www.socket.task.ITask;
import org.kaipan.www.socket.task.ITaskFactory;
import org.kaipan.www.socket.worker.MessageWorker;

/**
 * Socket core processor
 * 
 * @author will<pan.kai@icloud.com>
 */
public class SocketProcessor
{
    private Ssl ssl;
    
	private Server server;
	
	/**
	 * router for controller
	 */
	private IRouter router;
	
	private Queue<Socket> inSocketQueue;
	
	/**
	 * use a better / faster queue
	 *     you could use new LinkedList<>(), but thread not safe
	 */
	private Queue<Message> outboundMessageQueue;
    
	private MessageWorker messageWorker;
	
    private Map<Long, Socket> socketMap;
    
    private ByteBuffer readByteBuffer;
    private ByteBuffer writeByteBuffer;
    
    private Selector readSelector;
    private Selector writeSelector;
    
    private MessageBuffer readMessageBuffer;
    private MessageBuffer writeMessageBuffer;	
    
    private IMessageReaderFactory messageReaderFactory;
    
    private WriteProxy writeProxy;	
    
    private ITaskFactory taskFactory;
    
    /**
     * start incoming socket ids from 16K - reserve bottom ids for pre-defined sockets (servers).
     */
    private long nextSocketId;
    
    private Set<Socket> emptyToNonEmptySockets;
    private Set<Socket> nonEmptyToEmptySockets;
    
    private ExecutorService acceptThreadPool;
    
    public SocketProcessor(
    		Server server,
    		IRouter router,
			Queue<Socket>  socketQueue,
			Queue<Message> outboundMessageQueue,
			MessageWorker messageWorker,
			Map<Long, Socket> socketMap,
			ByteBuffer readByteBuffer,
			ByteBuffer writeByteBuffer,
			IMessageReaderFactory messageReaderFactory,
			Set<Socket> emptyToNonEmptySockets,
			Set<Socket> nonEmptyToEmptySockets,
			ExecutorService acceptThreadPool,
			ITaskFactory taskFactory)
    {
    	this.server = server;
    	this.router = router;
    	
    	this.inSocketQueue 		  = socketQueue;
    	this.outboundMessageQueue = outboundMessageQueue;
    	
    	this.messageWorker = messageWorker;
    	this.socketMap 	   = socketMap;
    	
    	this.readByteBuffer  = readByteBuffer;
    	this.writeByteBuffer = writeByteBuffer;
    	
    	this.messageReaderFactory = messageReaderFactory;
    	
    	this.emptyToNonEmptySockets = emptyToNonEmptySockets;
    	this.nonEmptyToEmptySockets = nonEmptyToEmptySockets;
    	
    	this.acceptThreadPool = acceptThreadPool;
    	
    	try {
            this.readSelector  = Selector.open();
            this.writeSelector = Selector.open();
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    	
    	this.readMessageBuffer  = new MessageBuffer();
    	this.writeMessageBuffer = new MessageBuffer();
    	
    	this.writeProxy  = new WriteProxy(this.writeMessageBuffer, this.outboundMessageQueue);
    	this.taskFactory = taskFactory;
    	
    	this.nextSocketId = 16 * 1024;
    }
    
    public static SocketProcessorBuilder custom() 
    {
    	return new SocketProcessorBuilder();
    }
    
    public void enSocketQueue(Socket socket) 
    {
    	inSocketQueue.add(socket);
    }
    
    private void registerRead(Socket socket) 
    {
        try {
            SocketChannel socketChannel = socket.getSocketChannel();
            
            if ( ! socketChannel.isOpen() ) return;
            
            socketChannel.configureBlocking(false);
            SelectionKey readKey = socketChannel.register(readSelector, SelectionKey.OP_READ);
            
            readKey.attach(socket);
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void registerWrite(Socket socket) 
    {
        try {
            SocketChannel socketChannel = socket.getSocketChannel();
            socketChannel.configureBlocking(false);
            SelectionKey writeKey = socketChannel.register(writeSelector, SelectionKey.OP_WRITE);
            
            writeKey.attach(socket);
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void takeNewInSocket() 
    {
    	Socket socket = inSocketQueue.poll();
    	
    	while ( socket != null ) {
    		this.nextSocketId++;
    		
    		Logger.write("client connected, socket id = " + this.nextSocketId);
    		
    		socket.setSocketId(this.nextSocketId);
    		socket.setMessageWriter(new MessageWriter());
    		
    		IMessageReader messageReader = messageReaderFactory.createMessageReader();
    		messageReader.initialize(readMessageBuffer);
    		
    		socket.setMessageReader(messageReader);
    		
    		// TLS/SSL protocol
    		if ( server.getConfig() instanceof SslConfig ) {
    		    SslConfig sslConfig = (SslConfig) server.getConfig();
    		  
    			if ( sslConfig.sslMode() ) {
    			    SocketChannel socketChannel = socket.getSocketChannel();
    			    
    			    try {
                        socketChannel.configureBlocking(false);
                    } 
    			    catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
    			    
    			    if ( ssl == null ) {
                        this.ssl = new Ssl(sslConfig.sslProtocol());
                        
                        ssl.init(sslConfig.sslServerCertsFile(), sslConfig.sslTrustsCertsFile(), 
                        		sslConfig.sslKeystorePassword(), sslConfig.sslKeyPassword());
                    }
    			   
    			    SslEngine sslEngine = new SslEngine(ssl.createSslEngine());   
    			    sslEngine.init(readByteBuffer, writeByteBuffer);
    			    		
    			    socket.setSslEngine(sslEngine);
    			    
    			    if ( ! sslEngine.doHandShake(socket) ) {
    			    	close(socket);
    			    	
    			    	Logger.write("client closed due to handshake failure, socket id = " + socket.getSocketId());
    			    }
    			}
    		}
    		
    		socketMap.put(this.nextSocketId, socket);
    		
    		registerRead(socket);
    		
    		// not thrown exception when queue is empty
    		socket = inSocketQueue.poll();
    	}
    }
    
    private void readFromSockets() 
    { 
       try {
           int readyChannels = readSelector.selectNow();
           
           if ( readyChannels == 0 ) return;
       } 
       catch (IOException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
       
       Set<SelectionKey>     selectedKeys = readSelector.selectedKeys();
       Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
       
       while ( keyIterator.hasNext() ) {
           SelectionKey key = keyIterator.next();
           
           if ( key.isReadable() ) {
               // a channel is ready for reading
               Socket socket = (Socket) key.attachment();
               
               IMessageReader messageReader  = socket.getMessageReader();
               boolean notEndOfStreamReached = messageReader.read(socket, readByteBuffer);
               
               if ( ! notEndOfStreamReached ) {
                   close(socket);
                   
                   Logger.write("client closed, socket id = " + socket.getSocketId());
               }
               
               List<Message> fullMessages = messageReader.getMessages();
               
               if ( fullMessages.size() > 0 ) {
                   for ( Message message : fullMessages ) {
                       message.socketId = socket.getSocketId();
                       
                       //ITask task = taskFactory.createTask(this, socket, message);
                       ITask task = taskFactory.createTask(server, socket, message);
                       messageWorker.addTask(task);
                   }
               }
               
               fullMessages.clear();
               
               /*
                * remove iteration just crossed elements,  
                *     to protect that key.isReadable() is always true    
                */
               keyIterator.remove();
           } 
       }
       
       selectedKeys.clear();
    }
    
    private void writeToSockets() 
    {
    	takeNewOutboundMessages();
    	
    	cancelEmptySockets();
    	
    	try {
			registerNonEmptySockets();
		} 
    	catch (ClosedChannelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	try {
			int writeChannels = writeSelector.selectNow();
			
			if ( writeChannels == 0 ) return;
		} 
    	catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        Set<SelectionKey>     selectedKeys = writeSelector.selectedKeys();
        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
        
        while ( keyIterator.hasNext() ) {
        	SelectionKey selectedKey = keyIterator.next();
        	
        	if ( selectedKey.isWritable() ) {
        		Socket socket 				= (Socket) selectedKey.attachment();
        		MessageWriter messageWriter = socket.getMessageWriter();
        		
        		try {
					messageWriter.write(socket, writeByteBuffer);
				} 
        		catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        		if ( socket.closeAfterResponse == true ) {
        			close(socket);
        			
        			Logger.write("close client, socket id = " + socket.getSocketId());
        		}
        		else if ( messageWriter.isEmpty() ) {
        			nonEmptyToEmptySockets.add(socket);
        		}
        	}
        	
        	keyIterator.remove();
        }
        
      selectedKeys.clear();
    }
    
    private void close(Socket socket) 
    {
        SelectionKey selectedKey;
    	SocketChannel socketChannel = socket.getSocketChannel();
		
    	selectedKey = socketChannel.keyFor(this.readSelector);
		if ( selectedKey != null ) {
			selectedKey.attach(null);
			selectedKey.cancel();
		}
		
		selectedKey = socketChannel.keyFor(this.writeSelector);
		if ( selectedKey != null ) {
			selectedKey.attach(null);
			selectedKey.cancel();
		}

		SslEngine sslEngine = socket.getSslEngine();
		if ( sslEngine != null ) {
			sslEngine.handleEndOfStream(socket);
		}
		
		try {
			socketChannel.close();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		socketMap.remove(socket.getSocketId());
    }
    
    private void registerNonEmptySockets() throws ClosedChannelException 
    {
        for ( Socket socket : emptyToNonEmptySockets ) {
        	registerWrite(socket);
        }
        
        emptyToNonEmptySockets.clear();
    }

    private void cancelEmptySockets() 
    {
        for ( Socket socket : nonEmptyToEmptySockets ) {
        	// unregister from write selector
            SelectionKey writeKey = socket.getSocketChannel().keyFor(this.writeSelector);	

            writeKey.attach(null);
            writeKey.cancel();
        }
        
        nonEmptyToEmptySockets.clear();
    }
    
    private void takeNewOutboundMessages() 
    {
    	Message outMessage = outboundMessageQueue.poll();
    	
    	while ( outMessage != null ) {
    		Socket socket = socketMap.get(outMessage.socketId);
    		
    		if ( socket != null ) {
    			MessageWriter messageWriter = socket.getMessageWriter();
    			
    			if ( messageWriter.isEmpty() ) {
    				messageWriter.enqueue(outMessage);
    				
    				// remove the operation of canceling write
    				nonEmptyToEmptySockets.remove(socket); 
    				
    				// register write operation, not necessary if removed from nonEmptyToEmptySockets in prev. statement.
                    emptyToNonEmptySockets.add(socket);   
    			}
    			else {
    				messageWriter.enqueue(outMessage);
    			}
    		}
    		else {
    		    nonEmptyToEmptySockets.remove(socket);
    		}
    		
    		outMessage = outboundMessageQueue.poll();
    	}
    }
    
    private void executeCycle()
    {
        takeNewInSocket();
        readFromSockets();
        writeToSockets();
    }

    public void run() 
    {
    	messageWorker.start();
    	
        while( true ) {
            executeCycle();
            
            try {
                Thread.sleep(1, 0);
            } 
            catch (InterruptedException e) {
            	Logger.write(e.getMessage());
            }
        }
    }
    
    public Server getServer() 
    {
    	return server;
    }
    
    public Map<Long, Socket> getSocketMap() 
    {
    	return socketMap;
    }
    
    public ExecutorService getAcceptThreadPool() 
    {
        return acceptThreadPool;
    }
    
    public Selector getReadSelector() 
    {
        return readSelector;
    }
    
    public Selector getWriteSelector() 
    {
        return writeSelector;
    }
    
    public WriteProxy getWriteProxy() 
    {
    	return writeProxy;
    }
    
    public IRouter getRouter() 
    {
    	return router;
    }
}
