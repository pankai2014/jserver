package org.kaipan.jserver.socket.task;

import org.kaipan.jserver.socket.core.Message;
import org.kaipan.jserver.socket.core.Server;
import org.kaipan.jserver.socket.core.Socket;
import org.kaipan.jserver.socket.core.SocketProcessor;
import org.kaipan.jserver.socket.protocol.websocket.ShakeHand;
import org.kaipan.jserver.socket.protocol.websocket.ShakeHandFactory;
import org.kaipan.jserver.socket.protocol.websocket.WsConfig;
import org.kaipan.jserver.socket.protocol.websocket.WsFrame;
import org.kaipan.jserver.socket.protocol.websocket.WsMessageReadBuffer;
import org.kaipan.jserver.socket.protocol.websocket.WsMessageReader;

public abstract class WsMessageTask implements Task
{
	private WsConfig config;
	
	private Socket  socket;
	private Message message;
	
	private SocketProcessor socketProcessor;
	
	public WsMessageTask(Server server, Socket socket, Message message) 
	{
		this.config = (WsConfig) server.getConfig();
		
		this.socket  = socket;
		this.message = message;
		
		this.socketProcessor = server.getSocketProcessor();
	}
	
	public void run()
	{
		WsMessageReader messageReader  = (WsMessageReader) socket.getMessageReader();
		WsMessageReadBuffer readBuffer = messageReader.getReadBuffer();
		
		/**
		 * refactoring
		 *     replace type code with class(218)
		 */
		ShakeHand processor = ShakeHandFactory.create(readBuffer.httpHandShake);
		processor.run(this);
		
		if ( readBuffer.httpHandShake != WsMessageReader.HANDSHAKE_COMPLETED ) {
			return;
		}
		
		WsFrame request = (WsFrame) message.metaData;
		request.setSocketId(message.socketId);
		
		onMessage(request);
	}

	protected void send(long requestId, byte[] data) 
	{
		Message message = socketProcessor.getWriteProxy().getMessage();
		
		message.socketId = requestId;
		message.writeToMessage(data);
		
		socketProcessor.getWriteProxy().enqueue(message);
	}
	
	public WsConfig getWsConfig() 
	{
		return config;
	}
	
	public SocketProcessor getSocketProcessor() 
	{
		return socketProcessor;
	}
	
	public Socket getSocket() 
	{
		return socket;
	}
	
	public Message getMessage() 
	{
		return message;
	}
	
	protected abstract void onMessage(WsFrame request);
}
