package org.kaipan.www.socket.task;

import org.kaipan.www.socket.core.Message;
import org.kaipan.www.socket.core.Socket;
import org.kaipan.www.socket.core.SocketProcessor;
import org.kaipan.www.socket.protocol.http.HttpHeader;
import org.kaipan.www.socket.protocol.http.HttpRequest;
import org.kaipan.www.socket.protocol.http.HttpResponse;
import org.kaipan.www.socket.protocol.http.HttpUtil;
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
	}
	
	private void shakingHands()
	{
		HttpHeader metaData = (HttpHeader) message.metaData;
        HttpRequest request = HttpUtil.parseHttpRequest(message, metaData);
        
        Message message = socketProcessor.getWriteProxy().getMessage();
        
        HttpResponse reponse = new HttpResponse();
        
        reponse.setHttpStatus(101);
        reponse.setHeader("Upgrade", "websocket");
        reponse.setHeader("Connection", "Upgrade");
        reponse.setHeader("Sec-WebSocket-Accept", "");
        reponse.setHeader("Sec-WebSocket-Protocol", "chat");
	}
	
	private void handshakeCompleted() 
	{
		
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
				break;
			case WsMessageReader.SHAKING_HANDS:
				shakingHands();
				break;
			case WsMessageReader.HANDSHAKE_COMPLETED:
				handshakeCompleted();
				break;
		}
	}
}
