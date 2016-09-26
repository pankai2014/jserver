package org.kaipan.www.socket.core;

public abstract class IConfig
{
    private String host = "0.0.0.0";
    
    private int port    = 80;
    
    private String  ssl_protocol  = "TLSv1.2";
    private boolean open_ssl_mode = false;
    
    public String host() 
    {
        return host;
    }
    
    public void host(String host) 
    {
        this.host = host;
    }
    
    public int port() 
    {
        return port;
    }
    
    public void port(int port)
    {
        this.port = port;
    }
    
    public boolean sslMode() 
    {
    	return open_ssl_mode;
    }
    
    public void sslMode(boolean open_ssl_mode) 
    {
    	this.open_ssl_mode = open_ssl_mode;
    }
    
    public String sslProtocol() 
    {
    	return ssl_protocol;
    }
    
    public void sslProtocol(String ssl_protocol) 
    {
    	this.ssl_protocol = ssl_protocol;
    }
}
