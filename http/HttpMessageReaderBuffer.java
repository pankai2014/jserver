package org.kaipan.www.sockets.http;

import java.nio.ByteBuffer;

public class HttpMessageReaderBuffer
{
    public boolean headerComplete  = false;
    public boolean bodycomplete    = false;
    
    public int expectContentLength = 0;
    
    public ByteBuffer detail       = null;
}
