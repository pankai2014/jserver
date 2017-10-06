package org.kaipan.jserver.socket.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.Properties;

import org.kaipan.jserver.socket.log.Logger;
import org.kaipan.jserver.socket.util.Util;

public abstract class Server
{	
    protected Config config = null;
    
    protected String path;
    protected String filename;
    
    protected SocketProcessor   socketProcessor = null;
    protected ServerSocketChannel socketChannel = null;
    
    public Server(Config config, String filename) 
    {
    	this(config, filename, null);
    }
    
    public Server(Config config, String filename, String path) 
    {
    	this.config = config;
    	
    	this.path 	  = path;
    	this.filename = filename;
    	
    	initialize();
    }
    
    private void initialize() 
    {
    	try {
            this.socketChannel = ServerSocketChannel.open();
        } 
        catch (IOException e) {
        	Logger.error(e.getStackTrace());
        }
    	
        Properties property = null;
        if ( path == null ) {
        	ClassLoader classLoader = getClass().getClassLoader();  
            property = Util.loadConfigFile(classLoader.getResource(filename).getFile());
        }
        else {
            property = Util.loadConfigFile(path + filename);
            if ( property == null ) {
            	Logger.info("Usage: java -jar java-server-{version}.jar "
            			+ "\"path to file http-server.properties\"");
                return;
            }
        }
        
        config.load(property);
    }
    
    private void listen(String ip, int port) 
    {
    	SocketAddress address = new InetSocketAddress(ip, port);
        
        try {
        	socketChannel.bind(address);
		} 
        catch (IOException e) {
			Logger.error(e.getStackTrace());
		}
        
        Logger.info("listen " + address + "...");
    }
    
    public void start()
    {   
    	listen(config.host(), config.port());
    	
    	createSocketProcessor();
    	
    	socketProcessor.getAcceptThreadPool().execute(new Accept(this));
    	socketProcessor.run();
    }
    
    public Config getConfig() 
    {
    	return config;
    }
    
    public SocketProcessor getSocketProcessor() 
    {
    	return socketProcessor;
    }
    
    protected abstract void createSocketProcessor();
}
