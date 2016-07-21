package org.kaipan.www.sockets.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.kaipan.www.sockets.IMessageReader;
import org.kaipan.www.sockets.Log;
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
            //byteBuffer.clear();
            //return false;
        }

        byteBuffer.flip();
        System.out.println(new String(nextMessage.sharedArray, nextMessage.offset, nextMessage.length));
        
        // TODO Reading data exceeds 1M
        nextMessage.writeToMessage(byteBuffer);
        
        // body isn't complete
        if ( buffer.headerComplete == true 
                && buffer.bodycomplete == false ) {
            if ( buffer.expectContentLength <= nextMessage.length - buffer.prevBodyEndIndex ) {
                buffer.bodycomplete = true;
                
                int length       = buffer.prevBodyEndIndex + buffer.expectContentLength;
                
                Message message  = messageBuffer.getMessage();
                message.metaData = new HttpHeader();
                message.writePartialMessageToMessage(nextMessage, length);
                
                completeMessages.add(nextMessage);
                nextMessage = message;
            }
            
            byteBuffer.clear();
            return true;
        }
        
        HttpHeader metaData   = (HttpHeader)nextMessage.metaData;
        buffer.headerComplete = HttpUtil.prepare(nextMessage.sharedArray, nextMessage.offset, nextMessage.length, metaData);
        
        Log.write("head completed yet ? : " + buffer.headerComplete);
        
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
            
            Message message  = messageBuffer.getMessage();
            message.metaData = new HttpHeader();
            
            buffer.bodycomplete        = true;
            message.writePartialMessageToMessage(nextMessage, endIndex);
            
            completeMessages.add(nextMessage);
            nextMessage = message;
        }
        else {
            buffer.bodycomplete        = false;
            buffer.prevBodyEndIndex    = endIndex;
            buffer.expectContentLength = endIndex - realIndex;
        }
        
        Log.write("body completed yet ? : " + buffer.bodycomplete);

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
