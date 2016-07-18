package org.kaipan.www.sockets;

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

public class SocketProcessor
{
	private Queue<Socket>  inSocketQueue   		= new ArrayBlockingQueue<Socket>(1024);
    private Queue<Message> outboundMessageQueue = new LinkedList<>();   //todo use a better / faster queue.
    
    private Map<Long, Socket> socketMap         = new HashMap<>();
    
    private ByteBuffer readByteBuffer  = ByteBuffer.allocate(1024 * 1024 * 1);
    private ByteBuffer writeByteBuffer = ByteBuffer.allocate(1024 * 1024 * 1);
    
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
    private Set<Socket> readEndOfStreamSockets = new HashSet<>();
    
    public SocketProcessor(IMessageReaderFactory messageReaderFactory, MessageBuffer readMessageBuffer, MessageBuffer writeMessageBuffer, IMessageProcessor messageProcessor) 
    {
    	this.messageReaderFactory = messageReaderFactory;
    	
        this.readMessageBuffer    = readMessageBuffer;
        this.writeMessageBuffer   = writeMessageBuffer;
        this.writeProxy           = new WriteProxy(this.writeMessageBuffer, this.outboundMessageQueue);
        
        this.messageProcessor 	  = messageProcessor;
        
        try {
            this.readSelector  = Selector.open();
            this.writeSelector = Selector.open();
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void enSocketQueue(Socket socket) 
    {
    	inSocketQueue.add(socket);
    }
    
    public void registerRead(Socket socket) 
    {
        try {
            SocketChannel channel = socket.getSocketChannel();
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
    		
    		socketMap.put(this.nextSocketId, inSocket);
    		
    		registerRead(inSocket);
    		
    		// not thrown exception when queue is empty
    		inSocket = inSocketQueue.poll();
    	}
    }
    
    public void readFromSockets() 
    {
    	cancelReadSockets();
    	
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
            	
            	IMessageReader messageReader = socket.getMessageReader();
            	
            	boolean result = messageReader.read(socket, readByteBuffer);
            	if ( result == false ) {
            		readEndOfStreamSockets.add(socket);
            		return;
            	}
            	
            	List<Message> fullMessages = messageReader.getMessages();
            	
            	if ( fullMessages.size() > 0 ) {
            		for ( Message message : fullMessages ) {
            			message.socketId = socket.getSocketId();
            			messageProcessor.process(message, writeProxy);
            		}
            	}
            	
            	fullMessages.clear();
            } 
            
            /**
             *  remove iteration just crossed elements,  
             *      why do this operation, key.isReadable() is always true?
             */
            //keyIterator.remove();
        }
        
        /**
         * delete all of the elements from this collection, 
         *      same as above!!!
         */
        //selectedKeys.clear();
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
        		
        		if ( messageWriter.isEmpty() )  {
        			nonEmptyToEmptySockets.add(socket);
        		}
        	}
        	
        	//keyIterator.remove();
        }
        
      //selectedKeys.clear();
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
            SelectionKey key = socket.getSocketChannel().keyFor(this.writeSelector);	// unregister from write selector

            key.cancel();
        }
        
        nonEmptyToEmptySockets.clear();
    }

    private void cancelReadSockets() 
    {
       for ( Socket socket : readEndOfStreamSockets ) {
    	    SelectionKey readKey = socket.getSocketChannel().keyFor(this.readSelector);    // unregister from read selector
	   	    readKey.cancel();
	   	    
	   	    try {
	   			socket.getSocketChannel().close();
	   			
	   			socket.setMessageReader(null);
	   			socket.setMessageWriter(null);
	   			socket = null;
	   		} 
	   	    catch (IOException e) {
	   			// TODO Auto-generated catch block
	   			e.printStackTrace();
	   		}
       }
       
       readEndOfStreamSockets.clear();
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
