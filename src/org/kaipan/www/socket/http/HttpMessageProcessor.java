package org.kaipan.www.socket.http;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.kaipan.www.socket.core.IMessageProcessor;
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
			FileInputStream in 	  	  = new FileInputStream(file);
			BufferedInputStream inBuf = new BufferedInputStream(in);
			
			length = file.length();
			bytes  = new byte[length.intValue()];
			
			while ( inBuf.read(bytes) != -1 );
			
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
	    
	    socket.closeAfterWriting = true;
	    writeProxy.enqueue(message);
	    
	    //Log.write("response: \n" + new String(message.sharedArray, message.offset, message.length));
	}
	
	public void doDynamicRequest(Socket socket, HttpRequest request, WriteProxy writeProxy) 
	{
		
	}
}
