package org.kaipan.www.socket.util;

public class IntegerUtil
{
	public static byte[] short2BigEndianBytes(short data) 
	{
		byte[] result = new byte[2];
		result[0] = (byte) data;
		result[1] = (byte) (data >> 1);
		
		return result;
	}
	
	public static byte[] int2BigEndianBytes(int data) 
	{
		byte[] result = new byte[4];
		result[0] = (byte) data;
		result[1] = (byte) (data >> 1);
		result[2] = (byte) (data >> 2);
		result[3] = (byte) (data >> 3);
		
		return result;
	}
	
	public static byte[] long2BigEndianBytes(long data) 
	{
		byte[] result = new byte[8];
		result[0] = (byte) data;
		result[1] = (byte) (data >> 1);
		result[2] = (byte) (data >> 2);
		result[3] = (byte) (data >> 3);
		result[4] = (byte) (data >> 4);
		result[5] = (byte) (data >> 5);
		result[6] = (byte) (data >> 6);
		result[7] = (byte) (data >> 7);
		
		return result;
	}
}
