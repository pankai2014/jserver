package org.kaipan.www.socket.protocol.websocket;

public class WsMessageReadBuffer
{	
	/**
	 * handshake state
	 * 
	 * 0: no handshake
	 * 
	 * 1: shaking hands
	 * 
	 * 2; handshake completed
	 */
	public int httpHandShake = 0;
}
