package org.kaipan.www.socket.http;

import java.io.File;
import java.io.UnsupportedEncodingException;

import org.kaipan.www.socket.core.IMessageProcessor;
import org.kaipan.www.socket.core.Log;
import org.kaipan.www.socket.core.Message;
import org.kaipan.www.socket.core.Socket;
import org.kaipan.www.socket.core.WriteProxy;
import org.kaipan.www.socket.util.Utils;

public class HttpMessageProcessor implements IMessageProcessor
{	
    private HttpConfig config;
    
	public HttpMessageProcessor(HttpConfig config) 
	{
		this.config = config;
	}
	
	@Override
	public void process(Socket socket, Message message, WriteProxy writeProxy) 
	{
	    HttpHeader metaData = (HttpHeader)message.metaData;
        HttpRequest request = HttpUtil.parseHttpRequest(message, metaData);
        
        if ( config.staticExt().contains(Utils.getFileExt(request.path)) ) {
            doStaticRequest(socket, request, writeProxy);
            return;
        }
        
        doDynamicRequest(socket, request, writeProxy);
	}
	
	public void doStaticRequest(Socket socket, HttpRequest request, WriteProxy writeProxy) 
	{
	    Message    message    = writeProxy.getMessage();
	    HttpResponse response = new HttpResponse();
	    
	    message.socketId    = request.socketId;
	    
	    String absolutePath = config.root() + request.path;
	   
	    File file = new File(absolutePath);
	    if ( ! file.exists() ) {
	        response.setHttpStatus(404);
	        
            try {
                message.writeToMessage(response.getHeader().getBytes(config.charset()));
                socket.closeAfterWriting = true;
                
                Log.write("response: \n" + new String(message.sharedArray, message.offset, message.length));
            } 
            catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
	    }
	    
	    writeProxy.enqueue(message);
	}
	
	public void doDynamicRequest(Socket socket, HttpRequest request, WriteProxy writeProxy) 
	{
		
	}
}
