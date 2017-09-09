package org.kaipan.www.socket.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.kaipan.www.socket.core.Stream;
import org.kaipan.www.socket.log.Logger;

public class SocketChannelClient implements Client
{
	public final static int READ_BUFFER_SIZE = 4194304;
	
	private SocketChannel client;
	
	public SocketChannelClient() 
	{
		this.initialize(false);
	}
	
	public SocketChannelClient(boolean blocking) 
	{
		this.initialize(blocking);
	}

	private void initialize(boolean blocking) 
	{
		try {
			client = SocketChannel.open();
			client.configureBlocking(blocking);
		} 
		catch (IOException e) {
			Logger.error(e.getStackTrace());
		}
	}
	
	@Override
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

	@Override
	public int read(ByteBuffer byteBuffer) throws IOException
	{
		return Stream.read(client, byteBuffer);
	}

	@Override
	public int write(ByteBuffer byteBuffer) throws IOException
	{
		return Stream.write(client, byteBuffer);
	}

	@Override
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
