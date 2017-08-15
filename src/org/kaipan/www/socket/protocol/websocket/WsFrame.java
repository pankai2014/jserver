package org.kaipan.www.socket.protocol.websocket;

public class WsFrame
{
	private boolean finish;
	
	private byte opcode;
	private byte[] data;
	
	private int length;
	
	public WsFrame setFinish(boolean finish) 
	{
		this.finish = finish;
		
		return this;
	}
	
	public WsFrame setOpcode(byte opcode) 
	{
		this.opcode = opcode;
		
		return this;
	}
	
	public WsFrame setData(byte[] data) 
	{
		this.data = data;
		
		return this;
	}
	
	public WsFrame setLength(int length) 
	{
		this.length = length;
		
		return this;
	}
	
	public boolean getFinish() 
	{
		return finish;
	}
	
	public byte getOpcode() 
	{
		return opcode;
	}
	
	public byte[] getData() 
	{
		return data;
	}
	
	public int getLength() 
	{
		return length;
	}
}
