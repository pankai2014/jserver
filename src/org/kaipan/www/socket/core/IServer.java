package org.kaipan.www.socket.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class IServer
{	
    protected IConfig iconfig = null;
    
    protected SocketProcessor   socketProcessor = null;
    protected ServerSocketChannel serverChannel = null;

    protected static final int acceptThreadSize = 10;
    
    private Lock lock = new ReentrantLock();
    
    protected IServer(IConfig iconfig)
    {
        this.iconfig = iconfig;
        
        try {
            this.serverChannel = ServerSocketChannel.open();
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        listen(iconfig.host(), iconfig.port());
    }
    
    public void listen(String ip, int port) 
    {
    	SocketAddress address = new InetSocketAddress(ip, port);
        
        try {
			serverChannel.bind(address);
		} 
        catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        Log.write("listen " + address + "...");
    }
    
    protected void accept() 
    {  
        try {
        	SocketChannel sockeChannel = serverChannel.accept();
            
        	Socket socket = new Socket(sockeChannel);
        	socketProcessor.enSocketQueue(socket);
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void start()
    {
        for ( int i = 0; i < acceptThreadSize; i++ ) {
        	new Thread(new Accept(this)).start();
        }
        
        socketProcessor.run();
    }
    
    public Lock getLock() 
    {
    	return lock;
    }
    
    protected abstract void createSocketProcessor();
    
}
