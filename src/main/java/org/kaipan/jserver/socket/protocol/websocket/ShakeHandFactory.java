package org.kaipan.jserver.socket.protocol.websocket;

public class ShakeHandFactory
{
	public static ShakeHand create(int status)
	{
		switch ( status ) {
			case WsMessageReader.NO_HANDSHAKE:
				return new NoShakeHand();
				
			case WsMessageReader.SHAKING_HANDS:
				return new ShakeHanding();
				
			case WsMessageReader.HANDSHAKE_COMPLETED:
				return new ShakeHandCompleted();
		}
		
		/**
		 * refactoring
		 *     replace error code with exception(310)
		 */
		throw new IllegalArgumentException("Incorrect status value");
	}
}
