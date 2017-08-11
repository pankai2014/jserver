package org.kaipan.www.socket.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;

import org.kaipan.www.socket.log.Logger;

public abstract class Server
{	
    protected Config config = null;
    
    protected SocketProcessor   socketProcessor = null;
    protected ServerSocketChannel socketChannel = null;
    
    protected Server(Config config)
    {
        this.config = config;
        
        try {
            this.socketChannel = ServerSocketChannel.open();
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        listen(config.host(), config.port());
    }
    
    public void listen(String ip, int port) 
    {
    	SocketAddress address = new InetSocketAddress(ip, port);
        
        try {
        	socketChannel.bind(address);
		} 
        catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        Logger.write("listen " + address + "...");
    }
    
    public void start()
    {   
    	socketProcessor.getAcceptThreadPool().execute(new Accept(this));
    	socketProcessor.run();
    }
    
    protected abstract void createSocketProcessor();
}