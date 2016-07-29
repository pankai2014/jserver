package org.kaipan.www.socket.http;

public class HttpMessageReaderBuffer
{
    public boolean headerComplete  = false;
    public boolean bodycomplete    = false;
    
    public int prevBodyEndIndex    = 0;
    public int expectContentLength = 0;
}
