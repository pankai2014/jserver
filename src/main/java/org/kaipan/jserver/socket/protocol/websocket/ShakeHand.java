package org.kaipan.jserver.socket.protocol.websocket;

import org.kaipan.jserver.socket.task.WsMessageTask;

public interface ShakeHand
{
	public void process(WsMessageTask task);
}
