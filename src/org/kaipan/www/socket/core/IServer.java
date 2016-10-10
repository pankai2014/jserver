package org.kaipan.www.socket.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;

public abstract class IServer
{	
    protected IConfig iconfig = null;
    
    protected SocketProcessor   processor = null;
    protected ServerSocketChannel channel = null;
    
    protected IServer(IConfig iconfig)
    {
        this.iconfig = iconfig;
        
        try {
            this.channel = ServerSocketChannel.open();
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
            channel.bind(address);
		} 
        catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        Log.write("listen " + address + "...");
    }
    
    public void start()
    {   
        processor.getCachedThreadPool().execute(new Accept(this));
        processor.run();
    }
    
    protected abstract void createSocketProcessor(IConfig iconfig);
}
