package org.kaipan.www.sockets;

public interface IMessageProcessor 
{
	public void process(Message message, WriteProxy writeProxy);
}
