package org.kaipan.www.socket.protocol.websocket;

import org.kaipan.www.socket.core.Message;
import org.kaipan.www.socket.task.WsMessageTask;

public class ShakeHandCompleted implements IShakeHand
{
	public ShakeHandCompleted() 
	{
		
	}
	
	@Override
	public void run(WsMessageTask task)
	{
		Message request = task.getMessage();
		Message message = task.getSocketProcessor().getWriteProxy().getMessage();
		
		message.socketId = request.socketId;
		message.writeToMessage(WsUtil.newCloseFrame());
		
		task.getSocketProcessor().getWriteProxy().enqueue(message);
	}
}
