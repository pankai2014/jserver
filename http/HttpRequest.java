package org.kaipan.www.sockets.http;

import java.util.Map;

public class HttpRequest
{
    public long socketId;
    
    public String method;
    public String uri;
    public String protocol;
    
    public Map<String, String> headers = null;

    public byte[] get;
    public byte[] post;
    public byte[] body;
    
    public byte[] file;
    public byte[] cookie;
    public byte[] session;
}
