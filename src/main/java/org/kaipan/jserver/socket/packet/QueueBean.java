package org.kaipan.jserver.socket.packet;

public class QueueBean
{
	public final int QUEUE_ADD_TYPE    = 1;
	public final int QUEUE_DELETE_TYPE = 2;
	public final int QUEUE_SHIFT_TYPE  = 3;
	public final int QUEUE_POP_TYPE    = 4;
	public final int QUEUE_ACK_TYPE    = 5;
	
	private int id;
	private int type;
	
	private String data;
	
	public int getId() 
	{
		return id;
	}
	
	public int getType() 
	{
		return type;
	}
	
	public String getData() 
	{
		return data;
	}
}
