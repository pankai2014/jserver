package org.kaipan.www.sockets;

class ServerTest
{
    public static void main(String[] args) 
    {
    	Server server = new Server("0.0.0.0", 8080);
        server.start();
    }
}
