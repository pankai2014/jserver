package org.kaipan.www.sockets.http;

import org.kaipan.www.sockets.IMessageProcessor;
import org.kaipan.www.sockets.Message;
import org.kaipan.www.sockets.WriteProxy;

public class HttpMessageProcessor implements IMessageProcessor
{	
	public HttpMessageProcessor() 
	{
		
	}
	
	@Override
	public boolean process(Message message, WriteProxy writeProxy) 
	{
	    HttpHeader metaData = (HttpHeader)message.metaData;
        HttpRequest request = HttpUtil.parseHttpRequest(message, metaData);
        
        doStaticRequest(request);
        
        return false;
	}
	
	public void doStaticRequest(HttpRequest request) 
	{
	    System.out.println(request.method);
	    System.out.println(request.uri);
	    System.out.println(request.protocol);
	}
	
	public void doDynamicRequest(HttpRequest request) 
	{
		
	}
}
