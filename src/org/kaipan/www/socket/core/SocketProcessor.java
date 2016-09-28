package org.kaipan.www.socket.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import org.kaipan.www.socket.ssl.Ssl;
import org.kaipan.www.socket.ssl.SslConfig;
import org.kaipan.www.socket.https.HttpSslConfig;

public class SocketProcessor
{
    private Ssl ssl = null;
    
	private IConfig iconfig = null;
	
	private Queue<Socket>  inSocketQueue   		= new ArrayBlockingQueue<Socket>(1024);
    private Queue<Message> outboundMessageQueue = new LinkedList<>();   //todo use a better / faster queue, thread not safe
    
    private Map<Long, Socket> socketMap         = new HashMap<>();
    
    private ByteBuffer readByteBuffer  = ByteBuffer.allocate(1024 * 1024 * 4);
    private ByteBuffer writeByteBuffer = ByteBuffer.allocate(1024 * 1024 * 4);
    
    private Selector readSelector  = null;
    private Selector writeSelector = null;
    
    private MessageBuffer readMessageBuffer	   = null;
    private MessageBuffer writeMessageBuffer   = null;	
    private IMessageProcessor messageProcessor = null;
    
    private IMessageReaderFactory messageReaderFactory = null;
    private WriteProxy			  writeProxy		   = null;	
    
    private long nextSocketId = 16 * 1024; //start incoming socket ids from 16K - reserve bottom ids for pre-defined sockets (servers).
    
    private Set<Socket> emptyToNonEmptySockets = new HashSet<>();
    private Set<Socket> nonEmptyToEmptySockets = new HashSet<>();
    
    public SocketProcessor(IConfig config) 
    {   
    	this.iconfig = config;
    	
        try {
            this.readSelector  = Selector.open();
            this.writeSelector = Selector.open();
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void init(IMessageReaderFactory messageReaderFactory, MessageBuffer readMessageBuffer, MessageBuffer writeMessageBuffer, IMessageProcessor messageProcessor) 
    {
    	this.readMessageBuffer    = readMessageBuffer;
        this.writeMessageBuffer   = writeMessageBuffer;
        
    	this.messageReaderFactory = messageReaderFactory;
    	this.messageProcessor 	  = messageProcessor;
    	
    	this.writeProxy        	  = new WriteProxy(this.writeMessageBuffer, this.outboundMessageQueue);
    }
    
    public void enSocketQueue(Socket socket) 
    {
    	inSocketQueue.add(socket);
    }
    
    public void registerRead(Socket socket) 
    {
        try {
            SocketChannel channel = socket.getSocketChannel();
            
            if ( ! channel.isOpen() ) return;
            
            channel.configureBlocking(false);
            SelectionKey  readKey = channel.register(readSelector, SelectionKey.OP_READ);
            
            readKey.attach(socket);
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void registerWrite(Socket socket) 
    {
        try {
            SocketChannel channel = socket.getSocketChannel();
            channel.configureBlocking(false);
            SelectionKey writeKey = channel.register(writeSelector, SelectionKey.OP_WRITE);
            
            writeKey.attach(socket);
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void takeNewInSocket() 
    {
    	Socket inSocket = inSocketQueue.poll();
    	
    	while ( inSocket != null ) {
    		this.nextSocketId++;
    		
    		inSocket.setSocketId(this.nextSocketId);
    		inSocket.setMessageWriter(new MessageWriter());
    		
    		IMessageReader messageReader = messageReaderFactory.createMessageReader();
    		messageReader.initialize(readMessageBuffer);
    		
    		inSocket.setMessageReader(messageReader);
    		
    		// TLS/SSL protocol
    		if ( iconfig instanceof SslConfig ) {
    		    HttpSslConfig SslConfig = (HttpSslConfig) iconfig;
    		  
    			if ( SslConfig.sslMode() ) {
    			    if ( ssl == null ) {
                        this.ssl = new Ssl(SslConfig.sslProtocol());
                        
                        this.ssl.init(SslConfig.sslServerCertsFile(), SslConfig.sslTrustsCertsFile(), 
                                SslConfig.sslKeystorePassword(), SslConfig.sslKeyPassword());
                    }
    			    else {
    			        
    			    }
    			}
    		}
    		
    		socketMap.put(this.nextSocketId, inSocket);
    		
    		registerRead(inSocket);
    		
    		// not thrown exception when queue is empty
    		inSocket = inSocketQueue.poll();
    		
    		Log.write("client connected, socketId = " + this.nextSocketId);
    	}
    }
    
    public void readFromSockets() 
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
                    socketMap.remove(socket.getSocketId());
                    key.attach(null);
                    key.cancel();
                    
                    try {
                        key.channel().close();
                    } 
                    catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                    Log.write("client closed, socketId = " + socket.getSocketId());
                }
            	
            	List<Message> fullMessages = messageReader.getMessages();
            	
            	if ( fullMessages.size() > 0 ) {
            		for ( Message message : fullMessages ) {
            			message.socketId = socket.getSocketId();
            			
            			messageProcessor.process(socket, message, writeProxy);
            		}
            	}
            	fullMessages.clear();
            	
            	/**
                 *  remove iteration just crossed elements,  
                 *      why do this operation, key.isReadable() is always true?
                 *      keyIterator is invalid?
                 */
                keyIterator.remove();
            } 
        }
        
        /**
         * delete all of the elements from this collection, 
         *      same as above!!!
         */
        selectedKeys.clear();
        
        for ( Map.Entry<Long, Socket> entry : socketMap.entrySet() ) {
            this.registerRead(entry.getValue());
        }
    }
    
    public void writeToSockets() 
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
        	SelectionKey key = keyIterator.next();
        	
        	if ( key.isWritable() ) {
        		Socket socket 				= (Socket) key.attachment();
        		MessageWriter messageWriter = socket.getMessageWriter();
        		
        		try {
					messageWriter.write(socket, writeByteBuffer);
				} 
        		catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        		if ( socket.closeAfterWriting == true ) {
        			close(socket);
        			
        			Log.write("close client, socketId = " + socket.getSocketId());
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
    	SocketChannel channel = socket.getSocketChannel();
		SelectionKey readKey  = channel.keyFor(this.readSelector);
		readKey.attach(null);
		readKey.cancel();
		
		SelectionKey writeKey = channel.keyFor(this.writeSelector);
		writeKey.attach(null);
		writeKey.cancel();
		
		try {
			channel.close();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
            SelectionKey writeKey = socket.getSocketChannel().keyFor(this.writeSelector);	// unregister from write selector

            writeKey.attach(null);
            writeKey.cancel();
        }
        
        nonEmptyToEmptySockets.clear();
    }
    
    public void takeNewOutboundMessages() 
    {
    	Message outMessage = outboundMessageQueue.poll();
    	
    	while ( outMessage != null ) {
    		Socket socket = socketMap.get(outMessage.socketId);
    		
    		if ( socket != null ) {
    			MessageWriter messageWriter = socket.getMessageWriter();
    			
    			if ( messageWriter.isEmpty() ) {
    				messageWriter.enqueue(outMessage);
    				
    				nonEmptyToEmptySockets.remove(socket); //remove the operation of canceling write 
                    emptyToNonEmptySockets.add(socket);    //register write operation, not necessary if removed from nonEmptyToEmptySockets in prev. statement.
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
    
    public void executeCycle() 
    {
    	takeNewInSocket();
        readFromSockets();
        writeToSockets();
    }

    public void run() 
    {
        while ( true ) {
            executeCycle();
            
            try {
                Thread.sleep(10);
            } 
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
