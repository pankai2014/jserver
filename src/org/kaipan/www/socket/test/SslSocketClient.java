package org.kaipan.www.socket.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class SslSocketClient 
{
	private Socket socket;
	
	public static final int HTTPS_PORT = 443;
	
	public SslSocketClient(String hostname) 
	{
		SocketFactory factory = SSLSocketFactory.getDefault();
		
		try {
			socket = factory.createSocket(hostname, HTTPS_PORT);
		} 
		catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void request() 
	{
		try {
			OutputStream os = socket.getOutputStream();
			
			PrintWriter pw = new PrintWriter(os);
			
			String command = "GET / HTTP/1.1\r\n\r\n";
			pw.print(command);
			pw.flush();
			
			InputStream 	is 	  = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader  br    = new BufferedReader(isr);
			
			String line;
			
			while ( (line = br.readLine()) != null ) {
				System.out.println(line);
			}
			
			pw.close();
			br.close();
			socket.close();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) 
	{
		String hostname;
		
		if ( args.length == 0 ) {
			hostname = "www.baidu.com";
		}
		else {
			hostname = args[0];
		}

		SslSocketClient client = new SslSocketClient(hostname);
		client.request();
	}
}
