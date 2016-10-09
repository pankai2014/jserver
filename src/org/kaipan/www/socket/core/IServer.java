package org.kaipan.www.socket.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public abstract class IServer
{	
    protected IConfig iconfig = null;
    
    protected SocketProcessor   socketProcessor = null;
    protected ServerSocketChannel serverChannel = null;
    
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
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                Selector acceptSelect = null;
                try {
                    acceptSelect = Selector.open();
                    serverChannel.configureBlocking(false);
                    
                    serverChannel.register(acceptSelect, SelectionKey.OP_ACCEPT);
                } 
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                while ( true ) {
                    try {
                        int connect = acceptSelect.select();
                        if ( connect == 0 ) return;
                        
                        Set<SelectionKey>     selectedKeys = acceptSelect.selectedKeys();
                        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                        
                        while ( keyIterator.hasNext() ) {
                            SelectionKey key = keyIterator.next();
                            
                            if ( key.isAcceptable() ) {
                                accept();
                            }
                        }
                        
                        keyIterator.remove();
                    } 
                    catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } 
                }
            }
            
        }).start();
        
        socketProcessor.run();
    }
    
    protected abstract void createSocketProcessor(IConfig iconfig);
}
