package org.kaipan.www.socket;

public interface IMessageProcessor 
{
	public boolean process(Message message, WriteProxy writeProxy);
}
