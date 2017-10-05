package org.kaipan.jserver.socket.protocol.http;

import org.kaipan.jserver.socket.core.MessageReader;
import org.kaipan.jserver.socket.core.MessageReaderFactory;

public class HttpMessageReaderFactory implements MessageReaderFactory 
{

    public HttpMessageReaderFactory() 
    {
    	
    }

    public MessageReader createMessageReader() 
    {
        return new HttpMessageReader();
    }
}