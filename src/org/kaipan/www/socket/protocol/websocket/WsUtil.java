package org.kaipan.www.socket.protocol.websocket;

import java.io.UnsupportedEncodingException;

import org.kaipan.www.socket.util.IntegerUtil;

public class WsUtil
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
	
	private static byte[] getLengthBytes(short length) 
	{
		return IntegerUtil.short2BigEndian(length);
	}
	
	private static byte[] getLengthBytes(long length) 
	{
		return IntegerUtil.long2BigEndian(length);
	}
	
	private static int getPayloadLength(int length) 
	{
		int Length;
		
		if ( length == 0x7e ) {
			Length = 0x7e;
		}
		else if ( length > 0x7f ) {
			Length = 0x7f;
		}
		else {
			Length = length;
		}
		
		return Length;
	}
	
	private static int getDataLength(byte[] data, int offset, int length) 
	{
		byte[] bytes = IntegerUtil.bigEndian2LittleEndian(data, offset, length);
		
		int Length = 0x00;
		
		for ( int i = 0; i < length; i++ ) {
			Length |= bytes[i];
		}
		
		return Length;
	}
	
	private static boolean isFinish(byte mask) 
	{
		if ( (mask & 0x80) > 0 ) {
			return true;
		}
		
		return false;
	}
	
	private static boolean isMask(byte mask) 
	{
		if ( (mask & 0x80) > 0 ) {
			return true;
		}
		
		return false;
	}
	
	private static byte[] parseMessage(byte[] message, byte[] mask) 
	{
		for ( int i = 0; i < message.length; i++ ) {
			message[i] = (byte) (message[i] ^ mask[i % 4]);
		}
		
		return message;
	}
	
	public static WsFrame parseFrame(byte[] data, int offset, int length) 
	{
		WsFrame frame = new WsFrame();
	
		int index = offset;
	
		if ( isFinish(data[index]) ) {
			frame.setFinish(true);
		}
		
		int fsv1 = data[index] & 0x40;
		int fsv2 = data[index] & 0x20;
		int fsv3 = data[index] & 0x10;
		
		if ( fsv1 == 1 || fsv2 == 1 || fsv3 == 1 ) {
			
		}
		
		frame.setOpcode((byte) (data[index] & 0x0f));
		
		index++;
		
		if ( ! isMask(data[index]) ) {
			throw new IllegalArgumentException("Mask not set");
		}
		
		int Length = data[index] & 0x7f;
		if ( Length == 0x7e ) {
			Length = getDataLength(data, index, 2);
			
			index += 2;
		}
		else if ( Length == 0x7f ) {
			Length = getDataLength(data, index, 8);
			
			index += 8;
		}
		
		frame.setLength(Length);
		
		byte[] mask = new byte[4];
		System.arraycopy(data, ++index, mask, 0, 4);
		index += 3;

		byte[] message = new byte[Length];
		System.arraycopy(data, ++index, message, 0, Length);
		index += Length - 1;
		
		frame.setData(parseMessage(message, mask));
		
		return frame;
	}
	
	public static WsFrame parseFrame(byte[] data) 
	{
		return parseFrame(data, 0, data.length);
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
		return buildFrame(IntegerUtil.int2BigEndian(data), opcode, close);
	}
	
	public static byte[] newFrame(byte[] data, int opcode, boolean close) 
	{
		return buildFrame(data, opcode, close);
	}
	
	public static byte[] newCloseFrame() 
	{
		return newFrame(String.valueOf(CLOSE_NORMAL).getBytes(), OPCODE_CLOSE, true);
	}
}
