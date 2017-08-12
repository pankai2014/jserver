package org.kaipan.www.socket.protocol.websocket;

public class ShakeHandFactory
{
	public static IShakeHand create(int statusCode)
	{
		switch ( statusCode ) {
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
		throw new IllegalArgumentException("Incorrect status code value");
	}
}
