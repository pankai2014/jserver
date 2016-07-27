package org.kaipan.www.socket.http;

import org.kaipan.www.socket.core.IMessageProcessor;
import org.kaipan.www.socket.core.Message;
import org.kaipan.www.socket.core.WriteProxy;

public class HttpMessageProcessor implements IMessageProcessor
{	
    private HttpConfig config;
    
	public HttpMessageProcessor(HttpConfig config) 
	{
		this.config = config;
	}
	
	@Override
	public void process(Message message, WriteProxy writeProxy) 
	{
	    HttpHeader metaData = (HttpHeader)message.metaData;
        HttpRequest request = HttpUtil.parseHttpRequest(message, metaData);
        
        doStaticRequest(request);
	}
	
	public void doStaticRequest(HttpRequest request) 
	{
	    System.out.println(request.method);
	    System.out.println(config.root() + request.path);
	    System.out.println(request.protocol);
	}
	
	public void doDynamicRequest(HttpRequest request) 
	{
		HttpResponse response = new HttpResponse();
	}
}
