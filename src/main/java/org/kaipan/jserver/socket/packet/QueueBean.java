package org.kaipan.jserver.socket.packet;

public class QueueBean
{
	public final static int QUEUE_ADD_TYPE    = 1;
	public final static int QUEUE_DELETE_TYPE = 2;
	public final static int QUEUE_SHIFT_TYPE  = 3;
	public final static int QUEUE_POP_TYPE    = 4;
	public final static int QUEUE_ACK_TYPE    = 5;
	
	private int id;
	private int type;
	
	private String data;
	
	public QueueBean setId(int id) 
	{
		this.id = id;
		
		return this;
	}
	
	public QueueBean setType(int type) 
	{
		this.type = type;
		
		return this;
	}
	
	public QueueBean setData(String data) 
	{
		this.data = data;
		
		return this;
	}
	
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
	
	public String toString() 
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append(id)
			.append(type).append(data);
		
		return builder.toString();
	}
}
