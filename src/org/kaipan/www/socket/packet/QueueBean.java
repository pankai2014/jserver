package org.kaipan.www.socket.packet;

public class QueueBean
{
	public final int QUEUE_ADD_TYPE    = 1;
	public final int QUEUE_DELETE_TYPE = 2;
	public final int QUEUE_SHIFT_TYPE  = 3;
	public final int QUEUE_POP_TYPE    = 4;
	public final int QUEUE_OK_TYPE     = 5;
	
	private int type;
	
	private String queueId;
	private String data;
}
