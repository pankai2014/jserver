package org.kaipan.www.socket.server;

import java.util.Properties;

import org.kaipan.www.socket.core.Server;
import org.kaipan.www.socket.core.SocketProcessor;
import org.kaipan.www.socket.log.Logger;
import org.kaipan.www.socket.protocol.websocket.WsConfig;
import org.kaipan.www.socket.protocol.websocket.WsMessageReaderFactroy;
import org.kaipan.www.socket.task.DefaultWsMessageTask;
import org.kaipan.www.socket.task.MessageTaskFactory;
import org.kaipan.www.socket.util.Util;

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
            String jarHome = Util.getJarHome(config);
            
            property = Util.loadConfigFile(jarHome + "/websocket-server.properties");
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
	public void createSocketProcessor()
	{
        this.socketProcessor = SocketProcessor.custom()
        		.setServer(this)
        		.setMessageReaderFactory(new WsMessageReaderFactroy())
        		.setTaskFactory(new MessageTaskFactory(DefaultWsMessageTask.class))
        		.build();
	}
}
