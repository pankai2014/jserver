package org.kaipan.jserver.socket.protocol.queue;

public interface Action
{
	public byte[] process(QueueManager manager, QueueBean bean);
}
