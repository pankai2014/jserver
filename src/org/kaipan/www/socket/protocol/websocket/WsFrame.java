package org.kaipan.www.socket.protocol.websocket;

import org.kaipan.www.socket.util.IntegerUtil;

public class WsFrame
{
	public final static byte OPCODE_CONTINUE = 0x00;
	public final static byte OPCODE_TEXT 	 = 0x01;
	public final static byte OPCODE_BINARY 	 = 0x02;
	public final static byte OPCODE_CLOSE	 = 0x08;
	public final static byte OPCODE_PING	 = 0x09;
	public final static byte OPCODE_PONG	 = 0x0A;
	
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
	
	private static byte[] getLengthBytes(short length) 
	{
		return IntegerUtil.short2BigEndianBytes(length);
	}
	
	private static byte[] getLengthBytes(long length) 
	{
		return IntegerUtil.long2BigEndianBytes(length);
	}
	
	private static int getPayloadLength(int length) 
	{
		int payloadLength;
		
		if ( length == 0x7e ) {
			payloadLength = 0x7e;
		}
		else if ( length > 0x7f ) {
			payloadLength = 0x7f;
		}
		else {
			payloadLength = length;
		}
		
		return payloadLength;
	}
	
	private static byte[] buildFrame(byte[] data, int opcode, boolean close) 
	{
		int total = 2 + data.length;
		if ( data.length == 0x7e ) {
			total += 2;
		}
		else if ( data.length >= 0x7f ) {
			total += 8;
		}

		byte[] frame = new byte[total];
		
		int index = 0;
		
		if ( close != true ) {
			frame[index] = (byte) (0x00 << 7 | (0x00 << 6) | (0x00 << 5) | (0x00 << 4) | opcode);
		}
		else {
			frame[index] = (byte) (0x01 << 7 | (0x00 << 6) | (0x00 << 5) | (0x00 << 4) | opcode);
		}
		
		frame[++index] = (byte) (0x00 << 7 | getPayloadLength(data.length));
		
		if ( data.length == 0x7e ) {
			System.arraycopy(getLengthBytes((short) data.length), 0, frame, ++index, 2);
			index += 1;
		}
		else if ( data.length > 0x7f ) {
			System.arraycopy(getLengthBytes(data.length), 0, frame, ++index, 8);
			index += 7;
		}
		
		System.arraycopy(data, 0, frame, ++index, data.length);
		index += data.length - 1;
		
		return frame;
	}
	
	public static byte[] newFrame(int data, int opcode, boolean close) 
	{
		return buildFrame(IntegerUtil.int2BigEndianBytes(data), opcode, close);
	}
	
	public static byte[] newFrame(byte[] data, int opcode, boolean close) 
	{
		return buildFrame(data, opcode, close);
	}
	
	public static byte[] newCloseFrame() 
	{
		return newFrame(CLOSE_NORMAL, OPCODE_CLOSE, true);
	}
}
