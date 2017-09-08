package org.kaipan.www.socket.task;

import org.kaipan.www.socket.core.Message;
import org.kaipan.www.socket.core.Server;
import org.kaipan.www.socket.core.Socket;

public interface TaskFactory
{
	public Task createTask(Server server, Socket socket, Message message);
}
