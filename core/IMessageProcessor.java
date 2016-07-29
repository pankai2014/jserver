package org.kaipan.www.socket.core;

public interface IMessageProcessor 
{
	public void process(Socket socket, Message message, WriteProxy writeProxy);
}
