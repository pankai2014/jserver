package org.kaipan.www.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.kaipan.www.sockets.http.HttpMessageProcessor;
import org.kaipan.www.sockets.http.HttpMessageReaderFactory;

public class Server
{	
    private SocketProcessor   socketProcessor = null;
    private ServerSocketChannel serverChannel = null;
    
    private Lock lock = new ReentrantLock();

    protected volatile int 	nextSocketId 	    = 0;
    protected static final int acceptThreadSize = 10;
    
    public Server()
    {
    	initialize();
    }
    
    public Server(String ip, int port) 
    {
    	initialize();
    	
    	listen(ip, port);
    }
    
    private void initialize() 
    {
    	this.socketProcessor = new SocketProcessor(new HttpMessageReaderFactory(), new MessageBuffer(), new MessageBuffer(), new HttpMessageProcessor()); 
    	
    	try {
			this.serverChannel = ServerSocketChannel.open();
		} 
    	catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
        
        System.out.println("listen " + address);
    }
    
    public void accept() 
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
}
