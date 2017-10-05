package org.kaipan.jserver.socket.task;

import org.kaipan.jserver.socket.core.Message;
import org.kaipan.jserver.socket.core.Server;
import org.kaipan.jserver.socket.core.Socket;

public interface TaskFactory
{
	public Task createTask(Server server, Socket socket, Message message);
}
