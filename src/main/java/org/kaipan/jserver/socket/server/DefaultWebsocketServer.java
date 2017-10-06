package org.kaipan.jserver.socket.server;

import org.kaipan.jserver.socket.core.Config;
import org.kaipan.jserver.socket.core.Server;
import org.kaipan.jserver.socket.core.SocketProcessor;
import org.kaipan.jserver.socket.protocol.websocket.WsMessageReaderFactroy;
import org.kaipan.jserver.socket.task.DefaultWsMessageTask;
import org.kaipan.jserver.socket.task.MessageTaskFactory;

public class DefaultWebsocketServer extends Server
{
	public DefaultWebsocketServer(Config config, String filename, String path) 
	{
		super(config, filename, path);
	}

	@Override
	public void createSocketProcessor()
	{
        this.socketProcessor = SocketProcessor.custom()
        		.setServer(this)
        		.setMessageReaderFactory(new WsMessageReaderFactroy())
        		.setTaskFactory(new MessageTaskFactory(DefaultWsMessageTask.class))
        		.build();
	}
}
