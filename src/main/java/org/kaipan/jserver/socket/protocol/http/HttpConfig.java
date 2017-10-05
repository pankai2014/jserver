package org.kaipan.jserver.socket.protocol.http;

import java.util.Properties;

import org.kaipan.jserver.socket.core.Config;

public class HttpConfig extends Config
{
	protected String root = "/";
    
    protected String static_ext  = null;
    protected String dynamic_ext = null;
    protected int    post_maxsize;
    
    protected String fastcgi_root = "/";
    protected String fastcgi_host = "127.0.0.1";
    protected int    fastcgi_port = 9000;
    
    public HttpConfig() 
    {
        
    }
    
    public HttpConfig(Properties property) 
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
        
        if ( property.getProperty("server.root") != null ) {
            this.root = property.getProperty("server.root");
        }
        else {
        	throw new IllegalArgumentException("Please set www-root directory");  
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
        
        if ( property.getProperty("server.fastcgi_root") != null ) {
            this.fastcgi_root = property.getProperty("server.fastcgi_root");
        }
        
        if ( property.getProperty("server.fastcgi_host") != null ) {
            this.fastcgi_host = property.getProperty("server.fastcgi_host");
        }
        
        if ( property.getProperty("server.fastcgi_port") != null ) {
            this.fastcgi_port = Integer.parseInt(property.getProperty("server.fastcgi_port"));
        }
        
        if ( property.getProperty("server.charset") != null ) {
            this.charset(property.getProperty("server.charset"));
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
    
    public String fastcgiRoot() 
    {
    	return fastcgi_root;
    }
    
    public String fastcgiHost() 
    {
    	return fastcgi_host;
    }
    
    public int fastcgiPort() 
    {
    	return fastcgi_port;
    }
    
    public int postMaxSize() 
    {
        return post_maxsize;
    }
}
