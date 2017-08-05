package org.kaipan.www.socket.protocol.websocket;

import java.util.Properties;

import org.kaipan.www.socket.core.IConfig;
import org.kaipan.www.socket.core.IServer;
import org.kaipan.www.socket.core.SocketProcessor;
import org.kaipan.www.socket.util.Utils;

public class WebsocketServer extends IServer
{
	public WebsocketServer(IConfig iconfig)
	{
		super(iconfig);
		
		createSocketProcessor();
	}
	
	public WsConfig getConfig() 
	{
	    return (WsConfig)iconfig;
	}

	@Override
	public void createSocketProcessor()
	{
        this.socketProcessor = SocketProcessor.custom()
        		.setIConfig(getConfig())
        		.setMessageReaderFactory(new WsMessageReaderFactroy())
        		.build();
	}
	
	public static void main(String[] args)
    {
		WsConfig config = new WsConfig();
        
        String path = null;
        if ( args.length > 0 ) path = args[0];
        
        Properties property = null;
        if ( path == null ) {
            String jarHome = Utils.getJarHome(config);
            
            property = Utils.loadConfigFile(jarHome + "/websocket-server.properties");
        }
        else {
            property = Utils.loadConfigFile(path);
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
