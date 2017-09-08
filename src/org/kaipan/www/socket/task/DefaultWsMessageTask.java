package org.kaipan.www.socket.task;

import org.kaipan.www.socket.core.Message;
import org.kaipan.www.socket.core.Server;
import org.kaipan.www.socket.core.Socket;
import org.kaipan.www.socket.log.Logger;
import org.kaipan.www.socket.protocol.websocket.WsFrame;
import org.kaipan.www.socket.protocol.websocket.WsUtil;

public class DefaultWsMessageTask extends WsMessageTask
{
	public DefaultWsMessageTask(Server server, Socket socket, Message message)
	{
		super(server, socket, message);
	}

	@Override
	protected void onMessage(WsFrame request)
	{
		Logger.write(new String(request.getData()), Logger.INFO);
		
		send(request.getSocketId(), WsUtil.newCloseFrame());
	}
}
