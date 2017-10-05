package org.kaipan.jserver.socket.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;

import org.kaipan.jserver.socket.log.Logger;

public abstract class Server
{	
    protected Config config = null;
    
    protected SocketProcessor   socketProcessor = null;
    protected ServerSocketChannel socketChannel = null;
    
    public Server()
    {
    	initialize();
    }
    
    public Server(Config config)
    {
        this.config = config;
        
        initialize();
    }
    
    public void load(Config config) 
    {
    	this.config = config;
    }
    
    private void initialize() 
    {
    	try {
            this.socketChannel = ServerSocketChannel.open();
        } 
        catch (IOException e) {
        	Logger.error(e.getStackTrace());
        }
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
    
    protected abstract void initialize(String path);
    
    protected abstract void createSocketProcessor();
}
