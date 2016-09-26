package org.kaipan.www.socket.http;

import java.util.Properties;

public class HttpSslConfig extends HttpConfig
{
	private String	ssl_protocol  = "TLSv1.2";
	private boolean open_ssl_mode = true;
    
    public HttpSslConfig() 
    {
        
    }
    
    public HttpSslConfig(Properties property) 
    {
        load(property);
    }
    
    public boolean sslMode() 
    {
    	return open_ssl_mode;
    }
    
    public String sslProtocol() 
    {
    	return ssl_protocol;
    }
}
