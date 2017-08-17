package org.kaipan.www.socket.protocol.websocket;

public class WsFrame
{
	public final static byte OPCODE_CONTINUE 		= 0x00;
	public final static byte OPCODE_TEXT 	 		= 0x01;
	public final static byte OPCODE_BINARY 	 		= 0x02;
	public final static byte OPCODE_CLOSE	 		= 0x08;
	public final static byte OPCODE_PING	 		= 0x09;
	public final static byte OPCODE_PONG	 		= 0x0A;
	
	public final static int CLOSE_NORMAL 	 	    = 1000;
	public final static int CLOSE_GOING_AWAY 	    = 1001;
	public final static int CLOSE_PROTOCOL_ERROR    = 1002;
	public final static int CLOSE_DATA_ERROR 	    = 1004;
	public final static int CLOSE_STATUS_ERROR 	    = 1005;
	public final static int CLOSE_ABNORMAL 		    = 1006;
	public final static int CLOSE_MESSAGE_ERROR     = 1007;
	public final static int CLOSE_POLICY_ERROR      = 1008;
	public final static int CLOSE_MESSAGE_TOO_BIG   = 1009;
	public final static int CLOSE_EXTENSION_MISSING = 1010;
	public final static int CLOSE_SERVER_ERROR  	= 1011;
	public final static int CLOSE_TLS  				= 1015;
	
	private long socketId;
	
	private byte opcode;
	private byte[] data;
	
	private boolean fin;
	private boolean complete;
	
	public WsFrame setSocketId(long socketId) 
	{
		this.socketId = socketId;
		
		return this;
	}
	
	public WsFrame setFin(boolean fin) 
	{
		this.fin = fin;
		
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
	
	public WsFrame setComplete(boolean complete) 
	{
		this.complete = complete;
		
		return this;
	}
	
	public long getSocketId()
	{
		return socketId;
	}
	
	public boolean isFin() 
	{
		return fin;
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
		if ( data == null ) return 0;
		
		return data.length;
	}
	
	public boolean isComplete() 
	{
		return complete;
	}
}
