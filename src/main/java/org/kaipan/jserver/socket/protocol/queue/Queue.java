package org.kaipan.jserver.socket.protocol.queue;

import org.kaipan.jserver.socket.packet.QueueBean;

public interface Queue
{
	public boolean add();
	public boolean delete();
	
	public QueueBean shift();
	public QueueBean pop();
}
