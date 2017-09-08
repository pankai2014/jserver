package org.kaipan.www.socket.protocol.websocket;

import org.kaipan.www.socket.task.WsMessageTask;

public interface ShakeHand
{
	public void run(WsMessageTask task);
}
