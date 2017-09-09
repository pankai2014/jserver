package org.kaipan.www.socket.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.kaipan.www.socket.ssl.SslEngine;

public class Socket
{
	private long socketId;
	
	private SslEngine 		   sslEngine = null;
	
    private MessageReader messageReader  = null;
    private MessageWriter  messageWriter = null;
    private SocketChannel  socketChannel = null;
    
    public boolean closeAfterResponse = false;
    
    public Socket() 
    {
    	
    }
    
    public Socket(SocketChannel sockChannel) 
    {
        this.socketChannel = sockChannel;
    }
    
    public int read(ByteBuffer byteBuffer) throws IOException 
    {   
    	return Stream.read(socketChannel, byteBuffer);
    }

    public int write(ByteBuffer byteBuffer) throws IOException 
    {
    	return Stream.write(socketChannel, byteBuffer);
    }
    
    public void setSocketId(long socketId) 
    {
    	this.socketId = socketId;
    }
    
    public void setSslEngine(SslEngine sslEngine) 
    {
    	this.sslEngine = sslEngine;
    }
    
    public void setSocketChannel(SocketChannel sockChannel) 
    {
    	this.socketChannel = sockChannel;
    }
    
    public void setMessageReader(MessageReader messageReader) 
    {
    	this.messageReader = messageReader;
    }
    
    public void setMessageWriter(MessageWriter messageWriter) 
    {
    	this.messageWriter = messageWriter;
    }
    
    public long getSocketId() 
    {
    	return socketId;
    }
    
    public SslEngine getSslEngine() 
    {
    	return sslEngine;
    }

    public MessageReader getMessageReader() 
    {
    	return messageReader;
    }
    
    public MessageWriter getMessageWriter() 
    {
    	return messageWriter;
    }
    
    public SocketChannel getSocketChannel() 
    {
        return socketChannel;
    }
}
