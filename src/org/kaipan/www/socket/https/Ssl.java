package org.kaipan.www.socket.https;

import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

public class Ssl 
{
	private SSLContext context;
	
	public Ssl(String protocol, String host, int port) 
	{
		try {
			context = SSLContext.getInstance(protocol);
		} 
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
}
