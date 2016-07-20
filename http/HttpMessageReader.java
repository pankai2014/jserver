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
	
	private HttpMessageReaderBuffer buffer = new HttpMessageReaderBuffer();;
	
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
        
        // TODO Reading data exceeds 1M
        nextMessage.writeToMessage(byteBuffer);
        
        // body isn't complete
        if ( buffer.headerComplete == true 
                && buffer.bodycomplete == false 
                && buffer.detail == null ) {
            Message message  = messageBuffer.getMessage();
            message.metaData = new HttpHeader();
            
            if ( buffer.expectContentLength <= nextMessage.length ) {
                buffer.bodycomplete = true;
                message.writePartialMessageToMessage(nextMessage, buffer.expectContentLength);
            }
            
            completeMessages.add(nextMessage);
            nextMessage = message;

            byteBuffer.clear();
            return true;
        }
        
        HttpHeader metaData = (HttpHeader)nextMessage.metaData;
        
        // header isn't complete, merge buffer and nextMessage
        if ( buffer.detail != null
                && buffer.headerComplete == false ) {
            this.merge(nextMessage);
        	buffer.headerComplete = HttpUtil.prepare(nextMessage, metaData);
        } 
        else {
            // the new request comes
            buffer.headerComplete = HttpUtil.prepare(nextMessage, metaData);
        }
        
        // header was still unfinished
        if ( ! buffer.headerComplete ) {
            if ( nextMessage.length > HttpUtil.HTTP_HEAD_MAXLEN ) {
                // TODO Write log, header is too large
                this.buffer.detail = null;
                
                byteBuffer.clear();
                return false;
            }
            
            if ( buffer.detail == null ) buffer.detail = ByteBuffer.allocate(HttpUtil.HTTP_HEAD_MAXLEN);
            buffer.detail.put(nextMessage.sharedArray, nextMessage.offset, nextMessage.length);
            
            byteBuffer.clear();
        	return true;
        }
        else {
            int headerLength = metaData.endOfHeader - nextMessage.offset;
            if ( headerLength > HttpUtil.HTTP_HEAD_MAXLEN ) {
                // TODO Write log, header is too large
                this.buffer.detail = null;
                
                byteBuffer.clear();
                return false;
            }
            
            buffer.detail         = null;
            buffer.headerComplete = true;
        }
        
        Message message  = messageBuffer.getMessage();
        message.metaData = new HttpHeader();
        
        int endIndex = ((HttpHeader)nextMessage.metaData).bodyEndIndex;
        int realIndex = nextMessage.offset + nextMessage.length;
        
        if ( endIndex <= realIndex ) { 
            buffer.bodycomplete        = true;
            message.writePartialMessageToMessage(nextMessage, endIndex);
        }
        else {
            buffer.bodycomplete        = false;
            buffer.expectContentLength = endIndex - realIndex;
        }
        
        completeMessages.add(nextMessage);
        nextMessage = message;

        byteBuffer.clear();
        return true;
    }
    
    private void merge(Message message) 
    {
        int srcLength  = this.buffer.detail.limit();
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
