package org.kaipan.www.socket.client;

public interface Client
{
	public boolean connect(String host, int port);
	
	public byte[] read();
	
	public boolean write(byte[] data);
	
	public boolean close();
}
