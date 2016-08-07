package org.kaipan.www.socket.http;

import java.util.Properties;

import org.kaipan.www.socket.core.IConfig;

public class HttpConfig extends IConfig
{
    private String root;
    
    private String static_ext  = null;
    private String dynamic_ext = null;
    
    private int post_maxsize;
    
    private String charset    = null;
    
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
        
        if ( property.getProperty("server.static_ext") != null ) {
            this.static_ext = property.getProperty("server.static_ext");
        }
        
        if ( property.getProperty("server.dynamic_ext") != null ) {
            this.dynamic_ext = property.getProperty("server.dynamic_ext");
        }
        
        if ( property.getProperty("server.post_maxsize") != null ) {
            this.post_maxsize = Integer.parseInt(property.getProperty("server.post_maxsize", "4194304"));
        }
        
        if ( property.getProperty("server.charset") != null ) {
            this.charset = property.getProperty("server.charset");
        }
    }
    
    public String root() 
    {
        return root;
    }
    
    public String staticExt() 
    {
        return static_ext;
    }
    
    public String dynamicExt() 
    {
        return dynamic_ext;
    }
    
    public int postMaxSize() 
    {
        return post_maxsize;
    }
    
    public String charset() 
    {
        return charset;
    }
}
