package org.kaipan.jserver.socket.protocol.queue;

public interface Action
{
	public final static int OK    = 000;
	public final static int EMPTY = 003;
	
	public final static byte[] OK_MSG    = new byte[]{OK,    000, 'O', 'K'};
	public final static byte[] EMPTY_MSG = new byte[]{EMPTY, 000, 'E', 'M', 'P', 'T', 'Y'};
	
	public byte[] process(QueueManager manager, QueueBean bean);
}
