package org.kaipan.www.socket.protocol.websocket;

import java.util.Properties;

import org.kaipan.www.socket.core.Config;

public class WsConfig extends Config
{
	public WsConfig() 
	{
		
	}
	
	public WsConfig(Properties property) 
	{
		load(property);
	}
	
	public void load(Properties property) 
    {
		if ( property.getProperty("server.bind") != null )
        {
            this.host(property.getProperty("server.bind"));
        }
        
        if ( property.getProperty("server.port") != null )
        {
            this.port(Integer.parseInt(property.getProperty("server.port")));
        }
        
        if ( property.getProperty("server.charset") != null ) {
            this.charset(property.getProperty("server.charset"));
        }
    }
}
