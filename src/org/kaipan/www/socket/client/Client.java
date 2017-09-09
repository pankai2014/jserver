package org.kaipan.www.socket.client;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface Client
{
	public boolean connect(String host, int port);
	
	public int read(ByteBuffer byteBuffer) throws IOException;
	
	public int write(ByteBuffer data) throws IOException;
	
	public void close();
}
