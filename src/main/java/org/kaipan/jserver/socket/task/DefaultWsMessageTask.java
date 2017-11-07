package org.kaipan.jserver.socket.task;

import org.kaipan.jserver.socket.core.Message;
import org.kaipan.jserver.socket.core.Server;
import org.kaipan.jserver.socket.core.Socket;
import org.kaipan.jserver.socket.log.Logger;
import org.kaipan.jserver.socket.protocol.websocket.WsFrame;
import org.kaipan.jserver.socket.protocol.websocket.WsUtil;

public class DefaultWsMessageTask extends WsMessageTask
{
	public DefaultWsMessageTask(Server server, Socket socket, Message message)
	{
		super(server, socket, message);
	}

	@Override
	protected void onMessage(WsFrame frame)
	{
		Logger.info(new String(frame.getData()));
		
		send(frame.getSocketId(), WsUtil.newCloseFrame(WsFrame.CLOSE_NORMAL));
	}
}
