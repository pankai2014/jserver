package org.kaipan.www.socket.protocol.http;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest
{
    public long socketId;
    
    public String method;
    public String path;
    public String protocol;
    
    public Map<String, String> header = new HashMap<>();
    
    public Map<String, String> Get  = new HashMap<>();
    public Map<String, String> Post = new HashMap<>();
    
    public byte[] body;
    
    public byte[] file;
    public byte[] cookie;
    public byte[] session;
}
