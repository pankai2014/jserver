package org.kaipan.www.socket.core;

import java.util.Map;

import org.kaipan.www.socket.controller.IController;

public interface IMessageProcessor 
{
	public void process(Message message, WriteProxy writeProxy, Map<String, IController> map);
}
