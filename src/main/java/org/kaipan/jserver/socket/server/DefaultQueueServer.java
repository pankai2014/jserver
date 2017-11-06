package org.kaipan.jserver.socket.server;

import org.kaipan.jserver.socket.core.Config;
import org.kaipan.jserver.socket.core.Server;
import org.kaipan.jserver.socket.core.SocketProcessor;
import org.kaipan.jserver.socket.protocol.websocket.WsMessageReaderFactroy;
import org.kaipan.jserver.socket.task.DefaultQueueMessageTask;
import org.kaipan.jserver.socket.task.MessageTaskFactory;

public class DefaultQueueServer extends Server
{
	public DefaultQueueServer(Config config, String filename, String path)
	{
		super(config, filename, path);
	}

	@Override
	protected void createSocketProcessor()
	{
		this.socketProcessor = SocketProcessor.custom()
        		.setServer(this)
        		.setMessageReaderFactory(new WsMessageReaderFactroy())
        		.setTaskFactory(new MessageTaskFactory(DefaultQueueMessageTask.class))
        		.build();
	}
}
