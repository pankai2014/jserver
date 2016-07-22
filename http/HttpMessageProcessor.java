package org.kaipan.www.socket.http;

import org.kaipan.www.socket.IMessageProcessor;
import org.kaipan.www.socket.Message;
import org.kaipan.www.socket.WriteProxy;

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
		HttpResponse response = new HttpResponse();
	}
}
