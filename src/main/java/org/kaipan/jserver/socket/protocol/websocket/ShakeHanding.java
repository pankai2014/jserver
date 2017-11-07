package org.kaipan.jserver.socket.protocol.websocket;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.kaipan.jserver.socket.core.Message;
import org.kaipan.jserver.socket.log.Logger;
import org.kaipan.jserver.socket.protocol.http.HttpRequest;
import org.kaipan.jserver.socket.protocol.http.HttpResponse;
import org.kaipan.jserver.socket.protocol.http.HttpUtil;
import org.kaipan.jserver.socket.task.WsMessageTask;
import org.kaipan.jserver.socket.util.Util;

import sun.misc.BASE64Encoder;

@SuppressWarnings("restriction")
public class ShakeHanding implements ShakeHand
{
	private static final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	
	private static final int WEBSOCKET_VERSION = 13;
	
	public ShakeHanding() 
	{
		
	}
	
	private String secWebSocketAccept(String key) 
	{
		String[] fields = {key, GUID};
		
		String encrypt = "";
		
		try {
			BASE64Encoder encoder = new BASE64Encoder();
			encrypt = encoder.encode(Util.sha1(String.join("", fields)));
		} 
		catch (NoSuchAlgorithmException e) {
			Logger.error(e.getStackTrace());
		}
		
		return encrypt;
	}
	
	public void process(WsMessageTask task)
	{
        HttpRequest request = HttpUtil.parseHttpRequest(task.getMessage());
        
        Message message = task.getSocketProcessor().getWriteProxy().getMessage();
        
        HttpResponse response = new HttpResponse();
        
        response.setHttpStatus(101);
        response.setHeader("Upgrade", "websocket");
        response.setHeader("Connection", "Upgrade");
        response.setHeader("Sec-WebSocket-Accept",  secWebSocketAccept(request.header.get("Sec-WebSocket-Key").trim()));
        response.setHeader("Sec-WebSocket-Version", String.valueOf(WEBSOCKET_VERSION));
        
        WsConfig config = task.getWsConfig();
        
        try {
			message.writeToMessage(response.getHeader().getBytes(config.charset()));
		} 
        catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        
        message.socketId = request.socketId;
        
        task.getSocketProcessor().getWriteProxy().enqueue(message);
	}
}
