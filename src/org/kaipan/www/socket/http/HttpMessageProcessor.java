package org.kaipan.www.socket.http;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.kaipan.www.socket.core.IMessageProcessor;
import org.kaipan.www.socket.core.Message;
import org.kaipan.www.socket.core.Socket;
import org.kaipan.www.socket.core.WriteProxy;
import org.kaipan.www.socket.fastcgi.Client;
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
        
        String ext = Utils.getFileExt(request.path);
        
        if ( config.staticExt().contains(ext) ) {
            doStaticRequest(socket, request, writeProxy);
        }
        else if ( config.dynamicExt().contains(ext) ) {
        	doDynamicRequest(socket, request, writeProxy);
        }
	}
	
	public void doStaticRequest(Socket socket, HttpRequest request, WriteProxy writeProxy) 
	{
	    Message    message    = writeProxy.getMessage();
	    HttpResponse response = new HttpResponse();
	    
	    message.socketId    = request.socketId;		// must be set!!!
	    
	    String absolutePath = config.root() + request.path;
	   
	    File file = new File(absolutePath);
	    if ( ! file.exists() ) {
	        response.setHttpStatus(404);
	        
            try {
                message.writeToMessage(response.getHeader().getBytes(config.charset()));
                socket.closeAfterWriting = true;
                
                //Log.write("response: \n" + new String(message.sharedArray, message.offset, message.length));
            } 
            catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            writeProxy.enqueue(message);
            return;
	    }
	    
	    Long length  = null;
	    byte[] bytes = null;
	    try {
			FileInputStream in 	  = new FileInputStream(file);
			BufferedInputStream is = new BufferedInputStream(in);
			
			length = file.length();
			bytes  = new byte[length.intValue()];
			
			while ( is.read(bytes) != -1 );
			
			in.close();
			
		} 
	    catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    response.setHttpStatus(200);
	    response.setHeader("Content-Length", length.toString());
	    response.setHeader("Content-Type", HttpResponse.HTTP_MIMES_TYPE.get(Utils.getFileExt(request.path)));
	    
	    try {
			message.writeToMessage(response.getHeader().getBytes(config.charset()));
			message.writeToMessage(bytes);
		} 
	    catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
	    bytes = null;
	    
	    socket.closeAfterWriting = true;
	    writeProxy.enqueue(message);
	    
	    //Log.write("response: \n" + new String(message.sharedArray, message.offset, message.length));
	}
	
	public void doDynamicRequest(Socket socket, HttpRequest request, WriteProxy writeProxy) 
	{
		Message   message     = writeProxy.getMessage();
		Message   nextMessage = writeProxy.getMessage();
	    HttpResponse response = new HttpResponse();
	    
	    nextMessage.socketId  = request.socketId;	// must be set!!!
	    
	    String absolutePath = config.fastcgiRoot() + request.path;
	    
	    Client fastCgiClient = new Client(config.fastcgiHost(), config.fastcgiPort());
	    fastCgiClient.initialize(message);
	    
	    Map<String, String> params = new HashMap<>();
        
        params.put("GATEWAY_INTERFACE", "FastCGI/1.0");
        params.put("REQUEST_METHOD", "GET");
        
        params.put("SCRIPT_FILENAME", absolutePath);
        
        params.put("SCRIPT_NAME", request.path);
        params.put("QUERY_STRING", "");
        
        params.put("REQUEST_URI", request.path);
        params.put("DOCUMENT_URI", request.path);
        params.put("SERVER_SOFTWARE", "php/fcgiclient");
        
        params.put("REMOTE_ADDR", config.fastcgiHost());
        params.put("REMOTE_PORT", config.fastcgiPort() + "");
        
        params.put("SERVER_ADDR", config.host());
        params.put("SERVER_PORT", config.port() + "");
        
        params.put("SERVER_NAME", "Will");
        params.put("SERVER_PROTOCOL", "HTTP/1.1");
        
        params.put("CONTENT_TYPE", "");
        params.put("CONTENT_LENGTH", "0");
	    
        int requestId = fastCgiClient.request(params, null);
        
	    fastCgiClient.waitForResponse(requestId);
	    
	    response.setHttpStatus(200);
	    
	    int endOfHeader;
	    while ( true ) {
	    	endOfHeader= HttpUtil.findNextLineBreak(message.sharedArray, 0, message.length);
	    	if ( endOfHeader != -1 ) break;
	    }
	    int LengthOfHeader = endOfHeader + 1;
	    
	    response.setHeader("Content-Length", (message.length - LengthOfHeader) + "");
	    //response.setHeader("Content-Type", new String(message.sharedArray, 0, LengthOfHeader));
	    
	    try {
	    	nextMessage.writeToMessage(response.getHeader().getBytes(config.charset()));
	    	nextMessage.writeToMessage(message.sharedArray, message.offset + LengthOfHeader, message.length - LengthOfHeader);
		} 
	    catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
	    //System.out.println(new String(nextMessage.sharedArray, nextMessage.offset, nextMessage.length));
	    
	    socket.closeAfterWriting = true;
	    writeProxy.enqueue(nextMessage);
	}
}
