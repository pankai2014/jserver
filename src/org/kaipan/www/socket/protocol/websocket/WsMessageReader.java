package org.kaipan.www.socket.protocol.websocket;

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
	
	public boolean read(Socket socket, ByteBuffer byteBuffer) 
	{
		if ( readBuffer.httpHandShake == NO_HANDSHAKE ) {
			if ( ! httpHandshake(socket, byteBuffer) ) {
				return false;
			}
		}
		
		readBuffer.httpHandShake = HANDSHAKE_COMPLETED;
		
		return true;
	}
	
	public WsMessageReadBuffer getReadBuffer() 
	{
		return readBuffer;
	}
}
