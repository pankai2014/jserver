package org.kaipan.www.socket.util;

public class IntegerUtil
{
	public static byte[] int2BigEndianBytes(int data) 
	{
		byte[] result = new byte[4];
		result[0] = (byte) data;
		result[1] = (byte) (data >> 1);
		result[2] = (byte) (data >> 2);
		result[3] = (byte) (data >> 3);
		
		return result;
	}
}
