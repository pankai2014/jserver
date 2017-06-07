package org.kaipan.www.socket.https;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.kaipan.www.socket.core.IMessageReader;
import org.kaipan.www.socket.core.Log;
import org.kaipan.www.socket.core.Message;
import org.kaipan.www.socket.core.MessageBuffer;
import org.kaipan.www.socket.core.Socket;
import org.kaipan.www.socket.http.HttpHeader;
import org.kaipan.www.socket.http.HttpMessageReaderBuffer;
import org.kaipan.www.socket.http.HttpUtil;
import org.kaipan.www.socket.ssl.SslEngine;

public class HttpsMessageReader implements IMessageReader
{
	private MessageBuffer    messageBuffer = null;
	private List<Message> completeMessages = new ArrayList<Message>();
	private Message		  	   nextMessage = null;
	
	private HttpMessageReaderBuffer buffer = new HttpMessageReaderBuffer();
	
	@Override
	public void initialize(MessageBuffer readMessageBuffer) 
	{
		this.messageBuffer = readMessageBuffer;				
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
        
        // TODO Reading data exceeds 1M
        nextMessage.writeToMessage(byteBuffer);
        
        // body isn't complete
        if ( buffer.headerComplete == true 
                && buffer.bodycomplete == false ) {
        	int realContentLength = nextMessage.length - buffer.prevBodyEndIndex;
        	
            if ( buffer.expectContentLength <= realContentLength ) {
                buffer.bodycomplete = true;
                
                completeMessages.add(nextMessage);
                
                if ( realContentLength > buffer.expectContentLength ) {
                	 int length       = buffer.prevBodyEndIndex + buffer.expectContentLength;
                     
                     Message message  = messageBuffer.getMessage();
                     message.metaData = new HttpHeader();
                     message.writePartialMessageToMessage(nextMessage, length - nextMessage.offset);
                     
                     nextMessage = message;
                }
                else {
                	nextMessage = null;
                }
            }
            
            byteBuffer.clear();
            return true;
        }
        
        HttpHeader metaData   = (HttpHeader)nextMessage.metaData;
        buffer.headerComplete = HttpUtil.prepare(nextMessage.sharedArray, nextMessage.offset, nextMessage.length, metaData);
        
        Log.write("head completed yet ? : " + buffer.headerComplete + ", socket id = " + socket.getSocketId());
        
        // header was still unfinished
        if ( ! buffer.headerComplete ) {
            if ( nextMessage.length > HttpUtil.HTTP_HEAD_MAXLEN ) {
                // TODO Write log, header is too large
                byteBuffer.clear();
                return false;
            }
            
            byteBuffer.clear();
        	return true;
        }
        else {
            int headerLength = metaData.endOfHeader - nextMessage.offset;
            if ( headerLength > HttpUtil.HTTP_HEAD_MAXLEN ) {
                // TODO Write log, header is too large
                byteBuffer.clear();
                return false;
            }
            
            buffer.headerComplete = true;
        }
        
        int endIndex  = metaData.bodyEndIndex;
        int realIndex = nextMessage.offset + nextMessage.length;
        if ( endIndex <= realIndex ) { 
            buffer.bodycomplete = true;
            
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
            buffer.bodycomplete        = false;
            buffer.prevBodyEndIndex    = endIndex;
            buffer.expectContentLength = endIndex - realIndex;
        }
        
        Log.write("body completed yet ? : " + buffer.bodycomplete + ", socket id = " + socket.getSocketId());

        byteBuffer.clear();
        return true;
    }

	@Override
	public List<Message> getMessages() 
	{
		return completeMessages;
	}
}
