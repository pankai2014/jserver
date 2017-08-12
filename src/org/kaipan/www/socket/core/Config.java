package org.kaipan.www.socket.core;

import java.util.Properties;

public abstract class Config
{
    private String host = "0.0.0.0";
    
    private int port = 80;
    
    private String  ssl_protocol  = "TLSv1.2";
    private boolean open_ssl_mode = false;
    
    private String charset = "UTF-8";
    
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
    
    public String charset() 
    {
    	return charset;
    }
    
    public void charset(String charset) 
    {
    	this.charset = charset;
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
    
    public abstract void load(Properties property);
}
