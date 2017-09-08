package org.kaipan.www.socket.util;

public class IntegerUtil
{
	public static byte[] short2BigEndian(short data) 
	{
		byte[] result = new byte[2];
		
		result[0] = (byte) data;
		result[1] = (byte) (data >> 1);
		
		return result;
	}
	
	public static byte[] int2BigEndian(int data) 
	{
		byte[] result = new byte[4];
		
		result[0] = (byte) data;
		result[1] = (byte) (data >> 1);
		result[2] = (byte) (data >> 2);
		result[3] = (byte) (data >> 3);
		
		return result;
	}
	
	public static byte[] long2BigEndian(long data) 
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
	
	public static byte[] bigEndian2LittleEndian(byte[] data) 
	{
		return bigEndian2LittleEndian(data, 0, data.length);
	}
	
	public static byte[] bigEndian2LittleEndian(byte[] data, int offset, int length) 
	{
		byte[] result = new byte[length];
		
		for ( int i = 0; i < length; i++ ) {
			result[i] = data[offset + (length - i - 1)];
		}
		
		return result;
	}
}
