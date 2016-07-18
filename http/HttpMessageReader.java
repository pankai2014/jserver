package org.kaipan.www.sockets.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.kaipan.www.sockets.IMessageReader;
import org.kaipan.www.sockets.Message;
import org.kaipan.www.sockets.MessageBuffer;
import org.kaipan.www.sockets.Socket;

public class HttpMessageReader implements IMessageReader
{
	private MessageBuffer    messageBuffer = null;
	private List<Message> completeMessages = new ArrayList<Message>();

	private Message		  	   nextMessage = null;
	
	private ByteBuffer        headerBuffer = null;
	
	@Override
	public void initialize(MessageBuffer readMessageBuffer) 
	{
		// TODO Auto-generated method stub
		this.messageBuffer = readMessageBuffer;
		this.nextMessage   = messageBuffer.getMessage();
		
		this.nextMessage.metaData = new HttpHeader();
	}
	
    @Override
    public boolean read(Socket socket, ByteBuffer byteBuffer)
    {
        // TODO Auto-generated method stub        
        try {
            socket.read(byteBuffer);
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        if ( socket.endOfStreamReached == true ) {
            byteBuffer.clear();
            return false;
        }

        byteBuffer.flip();
        
        // @todo reading data exceeds 1M
        nextMessage.writeToMessage(byteBuffer);
        
        boolean headerComplete;
        
        if ( headerBuffer.hasArray() ) {
        	headerBuffer.put(nextMessage.sharedArray, nextMessage.offset, nextMessage.length);
        	
        	nextMessage.sharedArray = headerBuffer.array();
        	nextMessage.offset      = 0;
        	nextMessage.length      = nextMessage.sharedArray.length;
        	nextMessage.capacity    = nextMessage.sharedArray.length;
        	nextMessage.setMessageBuffer(null);
        	
        	headerComplete = HttpParser.prepare(nextMessage, (HttpHeader)nextMessage.metaData);
        } 
        else {
            headerComplete = HttpParser.prepare(nextMessage, (HttpHeader)nextMessage.metaData);
        }
        
        if ( ! headerComplete ) {
            if ( headerBuffer.hasArray() 
                    && headerBuffer.position() > HttpParser.HTTP_HEAD_MAXLEN ) {
                // @todo write log
                this.headerBuffer = null;
                return false;
            }
            
            if ( headerBuffer == null ) headerBuffer = ByteBuffer.allocate(HttpParser.HTTP_HEAD_MAXLEN);
            
            headerBuffer.put(byteBuffer.array(), 0, byteBuffer.limit());
        	return true;
        }
        
        int endIndex  = ((HttpHeader)nextMessage.metaData).bodyEndIndex;
        int realIndex = nextMessage.offset + nextMessage.length;
        
        if ( endIndex <= realIndex ) {
            Message message  = messageBuffer.getMessage();
            message.metaData = new HttpHeader();
            
            message.writePartialMessageToMessage(nextMessage, endIndex);
            
            nextMessage = message;
            completeMessages.add(nextMessage);
        }

        byteBuffer.clear();
        return true;
    }

	@Override
	public List<Message> getMessages() 
	{
		// TODO Auto-generated method stub
		return completeMessages;
	}
}
