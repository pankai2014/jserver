package org.kaipan.www.socket.fastcgi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class Client
{
    private String host = null;
    
    private int port;
    private int connectTimeOut = 5;
    
    private Socket client = null;
    
    public final static int VERSION           = 1;
    
    public final static int BEGIN_REQUEST     = 1;
    public final static int ABORT_REQUEST     = 2;
    public final static int END_REQUEST       = 3;
    public final static int PARAMS            = 4;
    public final static int STDIN             = 5;
    public final static int STDOUT            = 6;
    public final static int STDERR            = 7;
    public final static int DATA              = 8;
    public final static int GET_VALUES        = 9;
    public final static int GET_VALUES_RESULT = 10;
    public final static int UNKNOWN           = 11;
    public final static int MAX               = Client.UNKNOWN; 
   
    public final static int ROLE_RESPONDER    = 1;
    public final static int ROLE_AUTHORIZER   = 2;
    public final static int ROLE_FILTER       = 3;
    
    public Client(String host, int port) 
    {
        this.host = host;
        this.port = port;
    }
    
    public void connect() 
    {
        client = new Socket();
        
        SocketAddress address = new InetSocketAddress(host, port);
        try {
            client.connect(address, connectTimeOut);
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void request() 
    {
        int requestId = 1;
        
        //buildPacket(Client.BEGIN_REQUEST, , requestId);
    }
    
    public void buidPacket(int type, byte[] content, int requestId) 
    {
        int clen = content.length;
        
        
    }
}
