package org.kaipan.www.socket.protocol.queue;

import org.kaipan.www.socket.packet.QueueBean;

public interface Queue
{
	public boolean add();
	public boolean delete();
	
	public QueueBean shift();
	public QueueBean pop();
}
