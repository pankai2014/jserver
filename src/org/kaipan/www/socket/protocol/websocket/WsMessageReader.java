package org.kaipan.www.socket.protocol.websocket;

import java.nio.ByteBuffer;

import org.kaipan.www.socket.core.Socket;
import org.kaipan.www.socket.protocol.http.HttpMessageReader;

public class WsMessageReader extends HttpMessageReader
{
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
			readBuffer.httpHandShaked = true;
		}
		
		return result;
	}
	
	public boolean read(Socket socket, ByteBuffer byteBuffer) 
	{
		if ( readBuffer.httpHandShaked == false ) {
			if ( ! httpHandshake(socket, byteBuffer) ) {
				return false;
			}
		}
		
		return true;
	}
}
