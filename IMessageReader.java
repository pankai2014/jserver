package org.kaipan.www.socket;

import java.nio.ByteBuffer;
import java.util.List;

public interface IMessageReader
{
	public void initialize(MessageBuffer readMessageBuffer);
	
    public boolean read(Socket socket, ByteBuffer byteBuffer);
    
    public List<Message> getMessages();
}
