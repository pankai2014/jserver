package org.kaipan.www.sockets;

public interface IMessageProcessor 
{
	public boolean process(Message message, WriteProxy writeProxy);
}
