package org.kaipan.jserver.socket.protocol.websocket;

import java.util.Properties;

import org.kaipan.jserver.socket.core.Config;

public class WsConfig extends Config
{
	private String queue_path;
	
	public WsConfig() 
	{
		
	}
	
	public WsConfig(Properties property) 
	{
		load(property);
	}
	
	public void load(Properties property) 
    {
		if ( property.getProperty("server.bind") != null ) {
            this.host(property.getProperty("server.bind"));
        }
        
        if ( property.getProperty("server.port") != null ) {
            this.port(Integer.parseInt(property.getProperty("server.port")));
        }
        
        if ( property.getProperty("server.queue_path") != null ) {
            this.port(Integer.parseInt(property.getProperty("server.queue_path")));
        }
        
        if ( property.getProperty("server.charset") != null ) {
            this.charset(property.getProperty("server.charset"));
        }
    }
	
	public String queuePath() 
	{
		return queue_path;
	}
}
