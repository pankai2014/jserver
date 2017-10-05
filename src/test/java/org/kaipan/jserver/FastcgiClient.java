package org.kaipan.jserver;

import java.util.HashMap;
import java.util.Map;

import org.kaipan.jserver.socket.client.fastcgi.Client;
import org.kaipan.jserver.socket.core.Message;
import org.kaipan.jserver.socket.core.MessageBuffer;

public class FastcgiClient
{
    public static void main(String[] args) 
    {
        Client client = new Client("127.0.0.1", 9000);
        MessageBuffer messageBuffer = new MessageBuffer();
        Message message = messageBuffer.getMessage();
        client.initialize(message);
        
        Map<String, String> params = new HashMap<String, String>();
        
        params.put("GATEWAY_INTERFACE", "FastCGI/1.0");
        params.put("REQUEST_METHOD", "GET");
        
        params.put("SCRIPT_FILENAME", "/home/will/Develop/projects/app/www/index.php");
        
        params.put("SCRIPT_NAME", "/index.php");
        params.put("QUERY_STRING", "");
        
        params.put("REQUEST_URI", "/index.php");
        params.put("DOCUMENT_URI", "/index.php");
        params.put("SERVER_SOFTWARE", "php/fcgiclient");
        
        params.put("REMOTE_ADDR", "127.0.0.1");
        params.put("REMOTE_PORT", "9000");
        
        params.put("SERVER_ADDR", "127.0.0.1");
        params.put("SERVER_PORT", "80");
        
        params.put("SERVER_NAME", "will-All-Series");
        params.put("SERVER_PROTOCOL", "HTTP/1.1");
        
        params.put("CONTENT_TYPE", "");
        params.put("CONTENT_LENGTH", "0");
        
        int requestId = client.request(params, null);
        
        client.waitForResponse(requestId);
    }
}
