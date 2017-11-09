package org.kaipan.jserver.socket.protocol.queue;

public class QueueBean
{
	public final static int QUEUE_PUSH_TYPE  = 1;
	public final static int QUEUE_POP_TYPE   = 2;
	public final static int QUEUE_ACK_TYPE   = 3;
	public final static int QUEUE_STATS_TYPE = 4;
	
	private int Id;
	private int type;
	
	private byte[] data;

	public QueueBean setId(int Id) 
	{
		this.Id = Id;
		
		return this;
	}
	
	public QueueBean setType(int type) 
	{
		this.type = type;
		
		return this;
	}
	
	public QueueBean setData(byte[] data) 
	{
		this.data = data;
		
		return this;
	}
	
	public int getId() 
	{
		return Id;
	}
	
	public int getType() 
	{
		return type;
	}
	
	public byte[] getData() 
	{
		return data;
	}

	public String toString() 
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append(Id).append("|")
			.append(type).append("|")
			.append(new String(data));
		
		return builder.toString();
	}
}
