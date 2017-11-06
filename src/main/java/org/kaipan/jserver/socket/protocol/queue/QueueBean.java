package org.kaipan.jserver.socket.protocol.queue;

public class QueueBean
{
	public final static int QUEUE_PUSH_TYPE = 1;
	public final static int QUEUE_POP_TYPE  = 2;
	public final static int QUEUE_ACK_TYPE  = 3;
	
	private int id;
	private int type;
	
	private byte[] data;

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
	
	public QueueBean setData(byte[] data) 
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
	
	public byte[] getData() 
	{
		return data;
	}

	public String toString() 
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append(id).append("|")
			.append(type).append("|")
			.append(new String(data));
		
		return builder.toString();
	}
}
