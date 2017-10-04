package org.kaipan.www.socket.server;

import java.util.Properties;

import org.kaipan.www.socket.controller.DefaultController;
import org.kaipan.www.socket.core.Server;
import org.kaipan.www.socket.core.SocketProcessor;
import org.kaipan.www.socket.log.Logger;
import org.kaipan.www.socket.protocol.http.HttpMessageReaderFactory;
import org.kaipan.www.socket.protocol.https.HttpsConfig;
import org.kaipan.www.socket.router.DynamicRouter;
import org.kaipan.www.socket.task.HttpsMessageTask;
import org.kaipan.www.socket.task.MessageTaskFactory;
import org.kaipan.www.socket.util.Util;

public class DefaultHttpsServer extends Server
{
	public DefaultHttpsServer(String path) 
	{
		super();
		
		initialize(path);
	}
    
    protected void initialize(String path) 
    {
    	config = new HttpsConfig();
        
        Properties property = null;
        if ( path == null ) {
            String jarHome = Util.getJarHome(config);
            
            property = Util.loadConfigFile(jarHome + "/https-server.properties");
        }
        else {
            property = Util.loadConfigFile(path);
            if ( property == null ) {
            	Logger.info("Usage: java -jar java-server-{version}.jar "
            			+ "\"path to file java-server.properties\"");
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
    		.setTaskFactory(new MessageTaskFactory(HttpsMessageTask.class))
    		.setRouter(router)
    		.build();
    }
}
