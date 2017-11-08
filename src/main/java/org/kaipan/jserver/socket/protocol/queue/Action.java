package org.kaipan.jserver.socket.protocol.queue;

public interface Action
{
	public final static int OK    = 0x00;
	public final static int EMPTY = 0x03;
	
	public final static byte[] OK_MSG    = new byte[]{OK,    0x00, 'O', 'K'};
	public final static byte[] EMPTY_MSG = new byte[]{EMPTY, 0x00, 'E', 'M', 'P', 'T', 'Y'};
	
	public byte[] process(QueueManager manager, QueueBean bean);
}
