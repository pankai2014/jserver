package org.kaipan.jserver.socket.core;

import java.nio.ByteBuffer;
import java.util.List;

public interface MessageReader
{
	public void initialize(MessageBuffer readMessageBuffer);
	
    public boolean read(Socket socket, ByteBuffer byteBuffer);
    
    public List<Message> getMessages();
}
