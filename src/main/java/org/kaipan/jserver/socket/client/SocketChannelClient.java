package org.kaipan.jserver.socket.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.kaipan.jserver.socket.log.Logger;

public class SocketChannelClient implements Client
{
	public final static int READ_BUFFER_SIZE = 4194304;
	
	private SocketChannel client;
	
	public SocketChannelClient() 
	{
		try {
			client = SocketChannel.open();
			client.configureBlocking(true);
		} 
		catch (IOException e) {
			Logger.error(e.getStackTrace());
		}
	}
	
	public boolean connect(String host, int port)
	{
		InetSocketAddress address = new InetSocketAddress(host, port);
		
		try {
			return client.connect(address);
		} 
		catch (IOException e) {
			Logger.error(e.getStackTrace());
		}
		
		return false;
	}

	public int read(ByteBuffer byteBuffer) throws IOException
	{
		return client.read(byteBuffer);
	}

	public int write(ByteBuffer byteBuffer) throws IOException
	{
		return client.write(byteBuffer);
	}

	public void close()
	{
		try {
			client.close();
		} 
		catch (IOException e) {
			Logger.error(e.getStackTrace());
		}
	}
}
