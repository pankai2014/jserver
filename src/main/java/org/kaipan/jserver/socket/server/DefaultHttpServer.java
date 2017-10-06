package org.kaipan.jserver.socket.server;

import org.kaipan.jserver.socket.controller.DefaultController;
import org.kaipan.jserver.socket.core.Config;
import org.kaipan.jserver.socket.core.Server;
import org.kaipan.jserver.socket.core.SocketProcessor;
import org.kaipan.jserver.socket.protocol.http.HttpMessageReaderFactory;
import org.kaipan.jserver.socket.router.DynamicRouter;
import org.kaipan.jserver.socket.task.HttpMessageTask;
import org.kaipan.jserver.socket.task.MessageTaskFactory;

public class DefaultHttpServer extends Server
{
	public DefaultHttpServer(Config config, String filename, String path) 
	{
		super(config, filename, path);
	}

    @Override
    protected void createSocketProcessor()
    {
    	DynamicRouter router = new DynamicRouter();
    	router.addMapping("/default", DefaultController.class);
    	
    	this.socketProcessor = SocketProcessor.custom()
    		.setServer(this)
    		.setMessageReaderFactory(new HttpMessageReaderFactory())
    		.setTaskFactory(new MessageTaskFactory(HttpMessageTask.class))
    		.setRouter(router)
    		.build();
    }
}
