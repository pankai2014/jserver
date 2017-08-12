package org.kaipan.www.socket.task;

import org.kaipan.www.socket.core.Message;
import org.kaipan.www.socket.core.Server;
import org.kaipan.www.socket.core.Socket;
import org.kaipan.www.socket.protocol.https.HttpsConfig;

public class HttpsMessageTask extends HttpMessageTask
{
	public HttpsMessageTask(Server server, Socket socket, Message message)
	{
		super(server, socket, message);
		
		this.config = (HttpsConfig) server.getConfig();
	}
}
