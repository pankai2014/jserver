package org.kaipan.jserver.socket.task;

import org.kaipan.jserver.socket.core.Message;
import org.kaipan.jserver.socket.core.Server;
import org.kaipan.jserver.socket.core.Socket;
import org.kaipan.jserver.socket.protocol.https.HttpsConfig;

public class HttpsMessageTask extends HttpMessageTask
{
	public HttpsMessageTask(Server server, Socket socket, Message message)
	{
		super(server, socket, message);
		
		this.config = (HttpsConfig) server.getConfig();
	}
}
