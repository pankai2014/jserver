package org.kaipan.jserver.socket.server;

import java.util.Properties;

import org.kaipan.jserver.socket.core.Server;
import org.kaipan.jserver.socket.core.SocketProcessor;
import org.kaipan.jserver.socket.log.Logger;
import org.kaipan.jserver.socket.protocol.websocket.WsConfig;
import org.kaipan.jserver.socket.protocol.websocket.WsMessageReaderFactroy;
import org.kaipan.jserver.socket.task.DefaultWsMessageTask;
import org.kaipan.jserver.socket.task.MessageTaskFactory;
import org.kaipan.jserver.socket.util.Util;

public class DefaultWebsocketServer extends Server
{
	public DefaultWebsocketServer(String path) 
	{
		super();
		
		initialize(path);
	}
	
    protected void initialize(String path) 
    {
    	config = new WsConfig();
        
        Properties property = null;
        if ( path == null ) {
            ClassLoader classLoader = getClass().getClassLoader();  
            property = Util.loadConfigFile(classLoader.getResource("websocket-server.properties").getFile());
        }
        else {
            property = Util.loadConfigFile(path);
            if ( property == null ) {
            	Logger.info("Usage: java -jar java-server-{version}.jar "
            			+ "\"path to file websocket-server.properties\"");
                return;
            }
        }
        
        config.load(property);
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
