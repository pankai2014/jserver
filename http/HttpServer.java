package org.kaipan.www.socket.http;

import org.kaipan.www.socket.MessageBuffer;
import org.kaipan.www.socket.Server;
import org.kaipan.www.socket.SocketProcessor;

public class HttpServer extends Server
{
    public HttpServer() 
    {
        super();
        
        createSocketProcessor();
    }
    
    public HttpServer(String ip, int port)
    {
        super(ip, port);

        createSocketProcessor();
    }

    @Override
    protected void createSocketProcessor()
    {
        this.socketProcessor = new SocketProcessor(new HttpMessageReaderFactory(), new MessageBuffer(), new MessageBuffer(), new HttpMessageProcessor()); 
    }
    
    public static void main(String[] args) 
    {
        HttpServer server = new HttpServer("0.0.0.0", 8080);
        server.start();
    }
}
