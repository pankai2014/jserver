package org.kaipan.www.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Client
{
    private String IP = "127.0.0.1";
    private int port  = 8080;
    
    private int SO_RCVBUF = 65535;
    
    public Client() 
    {
        try {
            SocketChannel client = SocketChannel.open();
            
            InetSocketAddress address = new InetSocketAddress(IP, port);
            if ( client.connect(address) ) {
                //Select selector = new Select();
                //selector.register(client, SelectionKey.OP_READ);
                
                //ByteBuffer byteBuffer = ByteBuffer.allocate(SO_RCVBUF);
              //used for writing data
                //byteBuffer.rewind();
                
                ByteBuffer byteBuffer = StringUtil.string2Bytes("hello\n");
                
                client.write(byteBuffer);
                
                client.close();
                
                //client.read(byteBuffer);
                
                //System.out.println(StringUtil.bytes2String(byteBuffer));
                
                //selector.start();
            }
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) 
    {
        new Client();
    }
}

