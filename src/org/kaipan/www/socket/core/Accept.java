package org.kaipan.www.socket.core;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Accept implements Runnable
{
    IServer server = null;
    
    public Accept(IServer server) 
    {
        this.server = server;
    }
    
    @Override
    public void run()
    {
        Selector acceptSelect = null;
        try {
            acceptSelect = Selector.open();
            server.channel.configureBlocking(false);
            
            server.channel.register(acceptSelect, SelectionKey.OP_ACCEPT);
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
                        try {
                            SocketChannel sockeChannel = server.channel.accept();
                            
                            Socket socket = new Socket(sockeChannel);
                            server.processor.enSocketQueue(socket);
                        } 
                        catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    
                    keyIterator.remove();
                }
                
                selectedKeys.clear();
            } 
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 
        }
    }
}
