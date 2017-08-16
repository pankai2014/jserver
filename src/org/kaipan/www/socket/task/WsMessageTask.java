package org.kaipan.www.socket.task;

import org.kaipan.www.socket.core.Message;
import org.kaipan.www.socket.core.Server;
import org.kaipan.www.socket.core.Socket;
import org.kaipan.www.socket.core.SocketProcessor;
import org.kaipan.www.socket.protocol.websocket.ShakeHandFactory;
import org.kaipan.www.socket.protocol.websocket.WsConfig;
import org.kaipan.www.socket.protocol.websocket.IShakeHand;
import org.kaipan.www.socket.protocol.websocket.WsMessageReadBuffer;
import org.kaipan.www.socket.protocol.websocket.WsMessageReader;

public class WsMessageTask implements ITask
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
	
	@Override
	public void run()
	{
		WsMessageReader messageReader  = (WsMessageReader) socket.getMessageReader();
		WsMessageReadBuffer readBuffer = messageReader.getReadBuffer();
		
		/**
		 * refactoring
		 *     replace type code with class(218)
		 */
		IShakeHand processor = ShakeHandFactory.create(readBuffer.httpHandShake);
		processor.run(this);
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
}
