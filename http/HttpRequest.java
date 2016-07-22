package org.kaipan.www.socket.http;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest
{
    public long socketId;
    
    public String method;
    public String uri;
    public String protocol;
    
    public Map<String, String> headers = new HashMap<>();

    public byte[] get;
    public byte[] post;
    public byte[] body;
    
    public byte[] file;
    public byte[] cookie;
    public byte[] session;
    
    public int expectLength = 0;
}
