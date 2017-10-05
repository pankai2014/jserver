package org.kaipan.jserver.socket.server;

import java.util.Properties;

import org.kaipan.jserver.socket.controller.DefaultController;
import org.kaipan.jserver.socket.core.Server;
import org.kaipan.jserver.socket.core.SocketProcessor;
import org.kaipan.jserver.socket.log.Logger;
import org.kaipan.jserver.socket.protocol.http.HttpConfig;
import org.kaipan.jserver.socket.protocol.http.HttpMessageReaderFactory;
import org.kaipan.jserver.socket.router.DynamicRouter;
import org.kaipan.jserver.socket.task.HttpMessageTask;
import org.kaipan.jserver.socket.task.MessageTaskFactory;
import org.kaipan.jserver.socket.util.Util;

public class DefaultHttpServer extends Server
{
	public DefaultHttpServer(String path) 
	{
		super();
		
		initialize(path);
	}
    
    protected void initialize(String path) 
    {
    	config = new HttpConfig();
        
        Properties property = null;
        if ( path == null ) {
        	ClassLoader classLoader = getClass().getClassLoader();  
            property = Util.loadConfigFile(classLoader.getResource("http-server.properties").getFile());
        }
        else {
            property = Util.loadConfigFile(path);
            if ( property == null ) {
            	Logger.info("Usage: java -jar java-server-{version}.jar "
            			+ "\"path to file http-server.properties\"");
                return;
            }
        }
        
        config.load(property);
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
