package org.kaipan.www.socket.core;

public abstract class IConfig
{
    private String host = "0.0.0.0";
    
    private int port    = 8080;
    
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
}
