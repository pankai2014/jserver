package org.kaipan.www.sockets.http;

import java.util.HashMap;
import java.util.Map;

import org.kaipan.www.sockets.IMessageProcessor;
import org.kaipan.www.sockets.Message;
import org.kaipan.www.sockets.WriteProxy;

public class HttpMessageProcessor implements IMessageProcessor
{	
    private final static int ST_FINISH = 1; 
    private final static int ST_WAIT   = 2; 
    
    private Map<Long, HttpRequest> request = new HashMap<>();
    
	public HttpMessageProcessor() 
	{
		
	}
	
	public int checkData(Message message, HttpRequest request) 
	{
	    HttpHeader metaData = (HttpHeader)message.metaData;
        
        Long socketId = new Long(message.socketId);
        
        if ( this.request.containsKey(socketId) ) {
            request = this.request.get(socketId);
            
            int offset = request.body.length - request.expectLength;
            if ( request.expectLength <= message.length ) {
                System.arraycopy(message, 0, request.body, offset, request.expectLength);
                return ST_FINISH;
            }
            
            request.expectLength = request.expectLength - message.length;
            
            System.arraycopy(message, 0, request.body, offset, message.length);
            
            return ST_WAIT;
        }
        
        request = HttpUtil.parseHttpRequest(message, metaData);
        
        if ( metaData.httpMethod == HttpHeader.HTTP_METHOD_POST ) {
          
            int realEndIndex = message.offset + message.length;
            if ( metaData.bodyEndIndex <= realEndIndex ) {
                return ST_FINISH;
            }
            
            this.request.put(socketId, request);
            
            return ST_WAIT;
        }
        
        return ST_FINISH;
	}
	
	@Override
	public void process(Message message, WriteProxy writeProxy) 
	{
	    HttpRequest request = null;
	    
	    int retCode = this.checkData(message, request);
	    switch( retCode ) {
            case ST_FINISH:
                response(request);
                break;
            case ST_WAIT:
                break;
	    }
	}
	
	public void response(HttpRequest request) 
	{
	    
	}
}
