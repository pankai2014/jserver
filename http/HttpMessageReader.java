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
	
	private HttpHeaderBuffer  headerBuffer = new HttpHeaderBuffer();;
	
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
        
        // body isn't complete
        if ( headerBuffer.headerComplete == true 
                && headerBuffer.bodycomplete == false 
                && headerBuffer.buffer == null ) {
            Message message  = messageBuffer.getMessage();
            message.metaData = new HttpHeader();
            
            if ( headerBuffer.expectContentLength <= nextMessage.length ) {
                headerBuffer.bodycomplete = true;
                message.writePartialMessageToMessage(nextMessage, headerBuffer.expectContentLength);
            }
            
            completeMessages.add(nextMessage);
            nextMessage = message;

            byteBuffer.clear();
            return true;
        }
        
        // header isn't complete, merge headerBuffer and nextMessage
        if ( headerBuffer.buffer != null
                && headerBuffer.headerComplete == false ) {
            this.merge(nextMessage);
        	headerBuffer.headerComplete = HttpParser.prepare(nextMessage, (HttpHeader)nextMessage.metaData);
        } 
        else {
            // the new request comes
            headerBuffer.headerComplete = HttpParser.prepare(nextMessage, (HttpHeader)nextMessage.metaData);
        }
        
        // header was still unfinished
        if ( ! headerBuffer.headerComplete ) {
            if ( nextMessage.length > HttpParser.HTTP_HEAD_MAXLEN ) {
                // @todo write log, header is too large
                this.headerBuffer.buffer = null;
                this.headerBuffer = null;
                
                byteBuffer.clear();
                return false;
            }
            
            if ( headerBuffer.buffer == null ) headerBuffer.buffer = ByteBuffer.allocate(HttpParser.HTTP_HEAD_MAXLEN);
            headerBuffer.buffer.put(nextMessage.sharedArray, nextMessage.offset, nextMessage.length);
            
            byteBuffer.clear();
        	return true;
        }
        else {
            if ( (((HttpHeader)nextMessage.metaData).endOfHeader - nextMessage.offset) 
                    > HttpParser.HTTP_HEAD_MAXLEN ) {
                // @todo write log, header is too large
                this.headerBuffer.buffer = null;
                this.headerBuffer = null;
                
                byteBuffer.clear();
                return false;
            }
            
            headerBuffer.buffer         = null;
            headerBuffer.headerComplete = true;
        }
        
        int endIndex  = ((HttpHeader)nextMessage.metaData).bodyEndIndex;
        int realIndex = nextMessage.offset + nextMessage.length;
        
        Message message  = messageBuffer.getMessage();
        message.metaData = new HttpHeader();
        if ( endIndex <= realIndex ) { 
            headerBuffer.bodycomplete        = true;
            message.writePartialMessageToMessage(nextMessage, endIndex);
        }
        else {
            headerBuffer.bodycomplete        = false;
            headerBuffer.expectContentLength = endIndex - realIndex;
        }
        
        completeMessages.add(nextMessage);
        nextMessage = message;

        byteBuffer.clear();
        return true;
    }
    
    private void merge(Message message) 
    {
        int srcLength  = this.headerBuffer.buffer.limit();
        int destLength = srcLength + message.length;
        
        byte[] dest = new byte[destLength];
        
        System.arraycopy(message.sharedArray, message.offset, dest, 0, destLength);
        
        nextMessage.sharedArray = dest;
        nextMessage.offset      = 0;
        nextMessage.length      = dest.length;
        nextMessage.capacity    = dest.length;
        nextMessage.setMessageBuffer(null);
    }

	@Override
	public List<Message> getMessages() 
	{
		// TODO Auto-generated method stub
		return completeMessages;
	}
}
