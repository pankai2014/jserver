package org.kaipan.www.socket.task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.kaipan.www.socket.client.fastcgi.Client;
import org.kaipan.www.socket.controller.Controller;
import org.kaipan.www.socket.core.Message;
import org.kaipan.www.socket.core.Server;
import org.kaipan.www.socket.core.Socket;
import org.kaipan.www.socket.core.SocketProcessor;
import org.kaipan.www.socket.log.Logger;
import org.kaipan.www.socket.protocol.http.HttpConfig;
import org.kaipan.www.socket.protocol.http.HttpRequest;
import org.kaipan.www.socket.protocol.http.HttpResponse;
import org.kaipan.www.socket.protocol.http.HttpUtil;
import org.kaipan.www.socket.util.Util;

public class HttpMessageTask implements Task
{
	protected HttpConfig config;

	private Socket  socket;
	private Message message;
	
	private SocketProcessor socketProcessor;
	
	public HttpMessageTask(Server server, Socket socket, Message message)
	{
		/**
		 * refactoring 
		 *     encapsulate downcast(308)
		 */
		this.config = (HttpConfig) server.getConfig();
		
		this.socket  = socket;
		this.message = message;
		
		this.socketProcessor = server.getSocketProcessor();
	}
	
	public void doStaticRequest(HttpRequest request) 
	{
	    Message    message    = socketProcessor.getWriteProxy().getMessage();
	    HttpResponse response = new HttpResponse();
	    
	    String absolutePath = config.root() + request.path;
	   
	    File file = new File(absolutePath);
	    if ( ! file.exists() ) {
	        response.setHttpStatus(404);
	        
            try {
                message.writeToMessage(response.getHeader().getBytes(config.charset()));
            } 
            catch (UnsupportedEncodingException e) {
                Logger.error(e.getStackTrace());
            }
            
            message.socketId = request.socketId;
            
            socketProcessor.getWriteProxy().enqueue(message);
            return;
	    }
	    
	    Long length  = null;
	    byte[] bytes = null;
	    try {
			FileInputStream in 	   = new FileInputStream(file);
			BufferedInputStream is = new BufferedInputStream(in);
			
			length = file.length();
			bytes  = new byte[length.intValue()];
			
			while ( is.read(bytes) != -1 );
			
			in.close();
			
		} 
	    catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	    catch (IOException e) {
			e.printStackTrace();
		}
	    
	    response.setHttpStatus(200);
	    response.setHeader("Content-Length", length.toString());
	    response.setHeader("Content-Type", HttpResponse.HTTP_MIMES_TYPE.get(Util.getFileExt(request.path)));
	    
	    try {
			message.writeToMessage(response.getHeader().getBytes(config.charset()));
			message.writeToMessage(bytes);
		} 
	    catch (UnsupportedEncodingException e) {
	    	Logger.error(e.getStackTrace());
		}
	    
	    message.socketId = request.socketId;	// must be set!!!
	    
	    socketProcessor.getWriteProxy().enqueue(message);
	}
	
	public void doDynamicRequest(HttpRequest request) 
	{
		Message   message     = socketProcessor.getWriteProxy().getMessage();
		Message   nextMessage = socketProcessor.getWriteProxy().getMessage();
		
	    HttpResponse response = new HttpResponse();
	    
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
	    
	    int endOfHeader = 0, loc = message.offset;
	    while ( true ) {
	        loc = HttpUtil.findNextLineBreak(message.sharedArray, loc, loc + message.length);
	        
	    	if ( loc != -1 ) {
	    	    endOfHeader = loc;
	    	    loc += 1; continue;
	    	}

	    	break;
	    }
	   
	    int LengthOfHeader = (endOfHeader - message.offset) + 1;
	    
	    response.setHeader("Content-Length", (message.length - LengthOfHeader) + "");
	    response.setHeader("Content-Type", HttpResponse.HTTP_MIMES_TYPE.get("html"));
	    
	    try {
	    	nextMessage.writeToMessage(response.getHeader().getBytes(config.charset()));
	    	nextMessage.writeToMessage(message.sharedArray, 
	    			message.offset + LengthOfHeader, message.length - LengthOfHeader);
		} 
	    catch (UnsupportedEncodingException e) {
	    	Logger.error(e.getStackTrace());
		}
	    
	    nextMessage.socketId = request.socketId;
	    
	    socketProcessor.getWriteProxy().enqueue(nextMessage);
	}
	
	public void doMapRequest(HttpRequest request) 
	{
		Message    message    = socketProcessor.getWriteProxy().getMessage();
	    HttpResponse response = new HttpResponse();
	    
	    String body = null;
	    
	    Controller controller = socketProcessor.getRouter().getController(request);
	    if ( controller != null ) {
	    	body = controller.run(request, response);
	    }
	
	    if ( body == null ) {
	    	body = "Oops, Something is wrong while processing the request " + request.path;
	    }
	    
	    Integer length = body.length();
	    
		response.setHttpStatus(200);
		response.setHeader("Content-Length", length.toString());
		response.setHeader("Content-Type", HttpResponse.HTTP_MIMES_TYPE.get("html"));
		
		try {
		 	message.writeToMessage(response.getHeader().getBytes(config.charset()));
		 	message.writeToMessage(body.getBytes());
		} 
	    catch (UnsupportedEncodingException e) {
	    	Logger.error(e.getStackTrace());
		}
		
		message.socketId = request.socketId;
		
		socketProcessor.getWriteProxy().enqueue(message);
	}

	@Override
	public void run()
	{
        HttpRequest request = HttpUtil.parseHttpRequest(message);
        
        String ext = Util.getFileExt(request.path);
        if ( ext == null ) {
        	socket.closeAfterResponse = true;
        	
        	doMapRequest(request);
        	return;
        }
        
        if ( config.staticExt().contains(ext) ) {
            doStaticRequest(request);
        }
        else if ( config.dynamicExt().contains(ext) ) {
        	doDynamicRequest(request);
        }
        
        socket.closeAfterResponse = true;
	}
}
