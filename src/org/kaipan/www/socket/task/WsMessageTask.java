package org.kaipan.www.socket.task;

import org.kaipan.www.socket.core.Message;
import org.kaipan.www.socket.core.Socket;
import org.kaipan.www.socket.core.SocketProcessor;
import org.kaipan.www.socket.protocol.websocket.HandshakeCompleted;
import org.kaipan.www.socket.protocol.websocket.NoShakeHand;
import org.kaipan.www.socket.protocol.websocket.ShakeHanding;
import org.kaipan.www.socket.protocol.websocket.WsMessageReadBuffer;
import org.kaipan.www.socket.protocol.websocket.WsMessageReader;

public class WsMessageTask implements ITask
{
	private Socket  socket;
	private Message message;
	
	private SocketProcessor socketProcessor;
	
	public WsMessageTask(SocketProcessor socketProcessor, Socket socket, Message message) 
	{
		this.socket  = socket;
		this.message = message;
		
		this.socketProcessor = socketProcessor;
	}
	
	@Override
	public void run()
	{
		WsMessageReader messageReader  = (WsMessageReader) socket.getMessageReader();
		WsMessageReadBuffer readBuffer = messageReader.getReadBuffer();
		
		/**
		 * TODO optimize
		 * reconstructed signal, polymorphism should be used instead
		 */
		switch ( readBuffer.httpHandShake ) {
			case WsMessageReader.NO_HANDSHAKE:
				new NoShakeHand(this).run();
				break;
			case WsMessageReader.SHAKING_HANDS:
				new ShakeHanding(this).run();
				break;
			case WsMessageReader.HANDSHAKE_COMPLETED:
				new HandshakeCompleted(this).run();
				break;
		}
	}
	
	public SocketProcessor getSocketProcessor() 
	{
		return socketProcessor;
	}
	
	public Message getMessage() 
	{
		return message;
	}
}
