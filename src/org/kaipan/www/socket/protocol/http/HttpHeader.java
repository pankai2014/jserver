package org.kaipan.www.socket.protocol.http;

public class HttpHeader 
{
    public final static int HTTP_METHOD_GET    = 1;
    public final static int HTTP_METHOD_POST   = 2;
    public final static int HTTP_METHOD_PUT    = 3;
    public final static int HTTP_METHOD_HEAD   = 4;
    public final static int HTTP_METHOD_DELETE = 5;

    public int httpMethod  	  = 0;
    public int endOfHeader    = 0;
    
    public int contentLength  = 0;

    public int bodyStartIndex = 0;
    public int bodyEndIndex   = 0;
}