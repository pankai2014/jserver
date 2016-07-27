package org.kaipan.www.socket.http;

import java.util.Properties;

import org.kaipan.www.socket.core.IConfig;

public class HttpConfig extends IConfig
{
    private String root;
    
    public HttpConfig() 
    {
        
    }
    
    public HttpConfig(Properties property) 
    {
        load(property);
    }
    
    public void load(Properties property) 
    {
        if ( property.getProperty("server.host") != null )
        {
            this.host(property.getProperty("server.host"));
        }
        
        if ( property.getProperty("server.port") != null )
        {
            this.port(Integer.parseInt(property.getProperty("server.port")));
        }
        
        if ( property.getProperty("server.root") != null ) {
            this.root = property.getProperty("server.root");
        }
        else {
            try {
                throw new Exception("please set www-root directory");
            } 
            catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }  
        }
    }
    
    public String root() 
    {
        return root;
    }
    
    public void root(String root) 
    {
        this.root = root;
    }
}
