package org.kaipan.www.socket.protocol.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.kaipan.www.socket.core.IMessageReader;
import org.kaipan.www.socket.core.Message;
import org.kaipan.www.socket.core.MessageBuffer;
import org.kaipan.www.socket.core.Socket;

public class HttpMessageReader implements IMessageReader
{
	protected MessageBuffer    messageBuffer = null;
	protected List<Message> completeMessages = null;
	protected Message		  	 nextMessage = null;
	
	protected HttpMessageReaderBuffer readBuffer = null;
	
	public HttpMessageReader()
	{
		readBuffer = new HttpMessageReaderBuffer();
		
		completeMessages = new ArrayList<Message>();
	}
	
	@Override
	public void initialize(MessageBuffer readMessageBuffer)
	{
		this.messageBuffer = readMessageBuffer;				
	}
	
	private boolean operate()
	{
		// body isn't complete
        if ( readBuffer.headerComplete == true
                && readBuffer.bodycomplete == false ) {
        	int realContentLength = nextMessage.length - readBuffer.prevBodyEndIndex;
        	
            if ( readBuffer.expectContentLength <= realContentLength ) {
                readBuffer.bodycomplete = true;
                
                completeMessages.add(nextMessage);
                nextMessage = null;
            }
            else {
            	int length       = readBuffer.prevBodyEndIndex + readBuffer.expectContentLength;
                
                Message message  = messageBuffer.getMessage();
                message.metaData = new HttpHeader();
                message.writePartialMessageToMessage(nextMessage, length - nextMessage.offset);
                
                nextMessage = message;
            }
            
            return true;
        }
        
        HttpHeader metaData = (HttpHeader) nextMessage.metaData;
        
        readBuffer.headerComplete = HttpUtil.prepare(nextMessage.sharedArray, 
        		nextMessage.offset, nextMessage.length, metaData);
        
        // header was still unfinished
        if ( ! readBuffer.headerComplete ) {
            if ( nextMessage.length > HttpUtil.HTTP_HEAD_MAXLEN ) {
                // TODO Write log, header is too large
                return false;
            }
            
        	return true;
        }
        else {
            int headerLength = metaData.endOfHeader - nextMessage.offset;
            if ( headerLength > HttpUtil.HTTP_HEAD_MAXLEN ) {
                // TODO Write log, header is too large
                return false;
            }
            
            readBuffer.headerComplete = true;
        }
        
        int endIndex  = metaData.bodyEndIndex;
        int realIndex = nextMessage.offset + nextMessage.length;
        if ( endIndex <= realIndex ) {
        	readBuffer.bodycomplete = true;
            
            completeMessages.add(nextMessage);
            
            if ( realIndex > endIndex ) {
            	Message message  = messageBuffer.getMessage();
                message.metaData = new HttpHeader();
            	
            	message.writePartialMessageToMessage(nextMessage, endIndex - nextMessage.offset);
            	nextMessage = message;
            }
            else {
            	nextMessage = null;
            }
        }
        else {
        	readBuffer.bodycomplete        = false;
        	readBuffer.prevBodyEndIndex    = endIndex;
        	readBuffer.expectContentLength = endIndex - realIndex;
        }
        
        return true;
	}
	
    @Override
    public boolean read(Socket socket, ByteBuffer byteBuffer)
    {
    	if ( nextMessage == null ) {
    		this.nextMessage 		  = messageBuffer.getMessage();
    		this.nextMessage.metaData = new HttpHeader();
    	}
    	
        try {
            socket.read(byteBuffer);
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        if ( socket.endOfStreamReached == true ) return false;

        byteBuffer.flip();
        
        // max reading data must be less than 4M
        nextMessage.writeToMessage(byteBuffer);
        byteBuffer.clear();
        
        return operate();
    }
    
    public HttpMessageReaderBuffer getReadBuffer() 
    {
    	return readBuffer;
    }

	@Override
	public List<Message> getMessages() 
	{
		return completeMessages;
	}
}
