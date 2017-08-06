package org.kaipan.www.socket.task;

import org.kaipan.www.socket.core.Message;
import org.kaipan.www.socket.core.SocketProcessor;

public interface ITaskFactory
{
	public ITask createTask(SocketProcessor socketProcessor, Message message);
}
