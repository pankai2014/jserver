package org.kaipan.www.socket.test;

import java.nio.ByteBuffer;

public class UnsignedByte 
{
	public void test2() 
	{
		//int clen = 0;
		
	    //clen = writeBytesBuffer.position() + 1;
		
//      addHeader(requestId, PARAMS, clen);
//      
//      writeBytesBuffer.flip();
//      System.out.println(clen);
//      System.out.println(writeBytesBuffer.remaining());
//      message.writeToMessage(writeBytesBuffer.array(), clen, HEADER_LEN);
//      message.writeToMessage(writeBytesBuffer.array(), 0, clen);
//      
//      writeBytesBuffer.clear();
	}

	public void test() 
	{
		byte[] bytes = new byte[8];
		
        int version       = (bytes[0] & 0xFF);
        int type		  = (bytes[1] & 0xFF);
        int responseId	  = ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);	// byte will be forced to transfer to int when calculating(!!!)
        int contentLength = ((bytes[4] & 0xFF) << 8) | (bytes[5] & 0xFF);   // bytes[2] & 0xFF, bytes[4] & 0xFF, (& 0xFF) must be set
        
        System.out.println("ResponseVersion: " + version);
        System.out.println("ResponseId: " + responseId);
        System.out.println("ResponseType: " + type);
        System.out.println("ResponseContentLength: " + contentLength);
        
//		buildNvpair("GATEWAY_INTERFACE", "FastCGI/1.0");
//		buildNvpair("REQUEST_METHOD", "GET");
//		buildNvpair("SCRIPT_FILENAME", "/home/will/Develop/projects/app/www/index.php");
//		buildNvpair("SCRIPT_NAME", "/index.php");
//		buildNvpair("QUERY_STRING", "");
//		buildNvpair("REQUEST_URI", "/index.php");
//		buildNvpair("DOCUMENT_URI", "/index.php");
//		buildNvpair("REMOTE_ADDR", "127.0.0.1");
//		buildNvpair("REMOTE_PORT", "9000");
//		buildNvpair("SERVER_ADDR", "127.0.0.1");
//		buildNvpair("SERVER_PORT", "8080");
//		buildNvpair("SERVER_NAME", "will-All-Series");
//		buildNvpair("SERVER_PROTOCOL", "HTTP/1.1");
//		buildNvpair("CONTENT_TYPE", "");
//		buildNvpair("CONTENT_LENGTH", "0");
        
    	//int responseId     = ((readBytes[i + 2] & 0xFFFF) << 8) + (readBytes[i + 3] & 0xFF);
    	//int contentLength  = ((readBytes[i + 4] & 0xFFFF) << 8) + (readBytes[i + 5] & 0xFF);
        
    	//int a = (readBytes[i + 2] & 0xFFFF) << 8;
//    	System.out.println(((readBytes[i + 2] << 8) & 0xFFFF));
//    	System.out.println(((readBytes[i + 3]) & 0xFF));
        
    	
//    	System.out.println("version: " + version);
//    	System.out.println("type: " + type);
//    	System.out.println("Id: " + responseId);
//    	System.out.println("Length: " + contentLength);
//    	System.out.println("padding: " + paddingLength);
//    	System.out.println("reserved: " + reserved);
//    	System.out.println();
	}
	
	public static boolean test3() 
	{
		byte[] bytes = new byte[4];
		
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		
		byteBuffer.putInt(65);
		
		System.out.println(bytes[3]);
		System.out.println("char: " + (char) bytes[3]);
		System.out.println("string: " + new String(bytes, 0, 4));
		
		return true;
	}
	
	public static void main(String[] args) 
	{
		if ( test3() ) {
			return;
		}
		
		// TODO Auto-generated method stub
        int Id = 257;
        byte m = (byte) ((Id >> 8) & 0xFF);
        byte n = (byte) ((Id)      & 0xFF);
        
        System.out.println("两个字节 257: ");
        int a = (m << 8) & 0xFFFF;
        int b = n & 0xff;
        
        System.out.println("第一字节: " + a);
        System.out.println("第二字节: " + b);
        
        
        System.out.println();
        System.out.println("四个字节 257: ");
        
        m = (byte) ((Id >> 24) | 0x80);
        n = (byte) ((Id >> 16) & 0xFF);
        byte p = (byte) ((Id >> 8)  & 0xFF);
        byte q = (byte) ((Id)       & 0xFF);
        
        System.out.println(m);
        System.out.println(n);
        System.out.println(p);
        System.out.println(q);
        
        a = (m & 0x7F ) << 24;
        b = (n & 0xFF ) << 16 ;
        int c = (p & 0xFF) << 8 ;
        int d = (q) & 0xFF;
        
        System.out.println("第一字节: " + a);
        System.out.println("第二字节: " + b);
        System.out.println("第三字节: " + c);
        System.out.println("第四字节: " + d);
        
	}

}
