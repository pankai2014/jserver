package org.kaipan.www.socket.protocol.https;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.kaipan.www.socket.core.IMessageReader;
import org.kaipan.www.socket.core.Message;
import org.kaipan.www.socket.core.MessageBuffer;
import org.kaipan.www.socket.core.Socket;
import org.kaipan.www.socket.protocol.http.HttpHeader;
import org.kaipan.www.socket.protocol.http.HttpMessageReader;
import org.kaipan.www.socket.protocol.http.HttpMessageReaderBuffer;
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
    	
    	SslEngine sslEngine = socket.getSslEngine();
    	if ( sslEngine == null ) return false;
    	
    	sslEngine.read(socket);
    	
        if ( socket.endOfStreamReached == true ) return false;

        byteBuffer.flip();
        
        // TODO Reading data exceeds 1M
        nextMessage.writeToMessage(byteBuffer);
        byteBuffer.clear();
        
        return HttpMessageReader.operate(messageBuffer, nextMessage, buffer, completeMessages);
    }

	@Override
	public List<Message> getMessages() 
	{
		return completeMessages;
	}
}