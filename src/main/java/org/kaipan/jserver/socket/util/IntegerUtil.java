package org.kaipan.jserver.socket.util;

/**
 * Integer small endian order is converted to big endian order
 * 
 * @author will<pan.kai@icloud.com>
 * @date   2017/10/15
 */
public class IntegerUtil
{
	public static byte[] short2BigEndian(short data) 
	{
		byte[] result = new byte[2];
		
		result[0] = (byte) data;
		result[1] = (byte) (data >> 8);
		
		return result;
	}
	
	public static byte[] int2BigEndian(int data) 
	{
		byte[] result = new byte[4];
		
		result[0] = (byte) data;
		result[1] = (byte) (data >> 8);
		result[2] = (byte) (data >> 16);
		result[3] = (byte) (data >> 24);
		
		return result;
	}
	
	public static byte[] long2BigEndian(long data) 
	{
		byte[] result = new byte[8];
		
		result[0] = (byte) data;
		result[1] = (byte) (data >> 8);
		result[2] = (byte) (data >> 16);
		result[3] = (byte) (data >> 24);
		result[4] = (byte) (data >> 32);
		result[5] = (byte) (data >> 40);
		result[6] = (byte) (data >> 48);
		result[7] = (byte) (data >> 56);
		
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
	
	public static short bigEndian2Short(byte[] bytes) 
	{
		return bigEndian2Short(bytes, 0);
	}
	
	public static short bigEndian2Short(byte[] bytes, int offset) 
	{
		short result = 0x00;
		
		for ( int i = 0; i < 2; i++ ) {
			result |= ((short) bytes[offset + i] & 0xff) << (i * 8);
		}
		
		return result;
	}
	
	public static int bigEndian2Int(byte[] bytes) 
	{
		return bigEndian2Int(bytes, 0);
	}
	
	public static int bigEndian2Int(byte[] bytes, int offset) 
	{
		int result = 0x00;
		
		for ( int i = 0; i < 4; i++ ) {
			result |= (bytes[offset + i] & 0xff) << (i * 8);
		}
		
		return result;
	}
	
	public static long bigEndian2Long(byte[] bytes) 
	{
		return bigEndian2Long(bytes, 0);
	}
	
	public static long bigEndian2Long(byte[] bytes, int offset) 
	{
		long result = 0x00;
		
		for ( int i = 0; i < 8; i++ ) {
			result |= ((long) bytes[offset + i] & 0xff) << (i * 8);
		}
		
		return result;
	}
}
