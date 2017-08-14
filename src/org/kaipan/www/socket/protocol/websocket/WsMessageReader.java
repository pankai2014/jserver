package org.kaipan.www.socket.protocol.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.kaipan.www.socket.core.Socket;
import org.kaipan.www.socket.protocol.http.HttpMessageReader;

public class WsMessageReader extends HttpMessageReader
{
	public final static int NO_HANDSHAKE  = 0;
	public final static int SHAKING_HANDS = 1;
	
	public final static int HANDSHAKE_COMPLETED = 2;
	
	private WsMessageReadBuffer readBuffer;
	
	public WsMessageReader()
	{
		super();
		
		readBuffer = new WsMessageReadBuffer();
	}
	
	private boolean httpHandshake(Socket socket, ByteBuffer byteBuffer) 
	{
		boolean result = super.read(socket, byteBuffer);
		
		if ( super.readBuffer.headerComplete == true ) {
			readBuffer.httpHandShake = SHAKING_HANDS;
		}
		
		return result;
	}
	
	private boolean operate() 
	{
		completeMessages.add(nextMessage);
        nextMessage = null;
		
		return true;
	}
	
	public boolean read(Socket socket, ByteBuffer byteBuffer) 
	{
		if ( readBuffer.httpHandShake == NO_HANDSHAKE ) {
			httpHandshake(socket, byteBuffer);
			
			return true;
		}
		
		readBuffer.httpHandShake = HANDSHAKE_COMPLETED;
		
		if ( nextMessage == null ) {
    		this.nextMessage = messageBuffer.getMessage();
    	}
		
		try {
            socket.read(byteBuffer);
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
		
		if ( socket.endOfStreamReached == true ) {
			return false;
		}
		
		byteBuffer.flip();
		
	    nextMessage.writeToMessage(byteBuffer);
        byteBuffer.clear();
		
		return operate();
	}
	
	public WsMessageReadBuffer getReadBuffer() 
	{
		return readBuffer;
	}
}
