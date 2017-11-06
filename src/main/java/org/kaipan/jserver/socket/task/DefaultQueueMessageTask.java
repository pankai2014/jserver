package org.kaipan.jserver.socket.task;

import org.kaipan.jserver.socket.core.Message;
import org.kaipan.jserver.socket.core.Server;
import org.kaipan.jserver.socket.core.Socket;
import org.kaipan.jserver.socket.log.Logger;
import org.kaipan.jserver.socket.protocol.queue.Queue;
import org.kaipan.jserver.socket.protocol.websocket.WsConfig;
import org.kaipan.jserver.socket.protocol.websocket.WsFrame;
import org.kaipan.jserver.socket.protocol.websocket.WsUtil;

public class DefaultQueueMessageTask extends WsMessageTask
{
	public DefaultQueueMessageTask(Server server, Socket socket, Message message)
	{
		super(server, socket, message);
	}

	@Override
	protected void onMessage(WsFrame request)
	{
		Logger.info(new String(request.getData()));
		
		WsConfig config = getWsConfig();
		Queue queue = Queue.getInstance(config.queuePath());
		if ( queue == null ) {
			send(request.getSocketId(), WsUtil.newCloseFrame());
		}
	}
}
