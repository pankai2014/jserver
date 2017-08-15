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
		
		byte[] frame = WsUtil.newCloseFrame();
		//byte[] frame = {(byte) 0x81, (byte) 0x05, (byte) 0x48, (byte) 0x65, (byte) 0x6c, (byte) 0x6c, (byte) 0x6f};
		
		message.socketId = request.socketId;
		message.writeToMessage(frame);
		
		task.getSocketProcessor().getWriteProxy().enqueue(message);
	}
}
