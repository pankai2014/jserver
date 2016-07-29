package org.kaipan.www.socket.core;

import java.util.concurrent.locks.Lock;

public class Accept implements Runnable
{

    private Server server = null;
    
    public Accept(Server server) 
    {
        this.server = server;
    }

    @Override
    public void run()
    {
        while ( true ) {
        	Lock lock = server.getLock();
            lock.lock();
            
            try {
                server.accept();
            }
            finally {
                lock.unlock();
            }
        }
    }
}
