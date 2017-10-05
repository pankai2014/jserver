package org.kaipan.jserver;

import java.security.NoSuchAlgorithmException;

import org.kaipan.jserver.socket.util.Util;

import sun.misc.BASE64Encoder;

@SuppressWarnings("restriction")
public class Ecrypt
{
	private static void sha1() 
	{
		String data = "UR7KDm/OaaKB/x3tvlXfcA==258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
		
		BASE64Encoder encoder = new BASE64Encoder();
		String encrypt = "";
		try {
			encrypt = encoder.encode(Util.sha1(data));
		} 
		catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(encrypt);
	}
	
	public static void main(String[] args)
	{
		sha1();
	}
}
