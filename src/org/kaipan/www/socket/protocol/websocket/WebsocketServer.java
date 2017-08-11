package org.kaipan.www.socket.protocol.websocket;

import java.util.Properties;

import org.kaipan.www.socket.core.Config;
import org.kaipan.www.socket.core.Server;
import org.kaipan.www.socket.core.SocketProcessor;
import org.kaipan.www.socket.task.MessageTaskFactory;
import org.kaipan.www.socket.task.WsMessageTask;
import org.kaipan.www.socket.util.Util;

public class WebsocketServer extends Server
{
	public WebsocketServer(Config iconfig)
	{
		super(iconfig);
		
		createSocketProcessor();
	}
	
	public WsConfig getConfig() 
	{
	    return (WsConfig) config;
	}

	@Override
	public void createSocketProcessor()
	{
        this.socketProcessor = SocketProcessor.custom()
        		.setConfig(getConfig())
        		.setMessageReaderFactory(new WsMessageReaderFactroy())
        		.setTaskFactory(new MessageTaskFactory(WsMessageTask.class))
        		.build();
	}
	
	public static void main(String[] args)
    {
		WsConfig config = new WsConfig();
        
        String path = null;
        if ( args.length > 0 ) path = args[0];
        
        Properties property = null;
        if ( path == null ) {
            String jarHome = Util.getJarHome(config);
            
            property = Util.loadConfigFile(jarHome + "/websocket-server.properties");
        }
        else {
            property = Util.loadConfigFile(path);
            if ( property == null ) {
                System.out.println("Usage: java -jar websocket-server-{version}.jar "
                        + "\"path to file websocket-server.properties\"");
                return;
            }
        }
        
        config.load(property);
        
        WebsocketServer server = new WebsocketServer(config);
        server.start();
    }
}