package org.kaipan.www.socket.protocol.http;

import org.kaipan.www.socket.core.MessageReader;
import org.kaipan.www.socket.core.MessageReaderFactory;

public class HttpMessageReaderFactory implements MessageReaderFactory 
{

    public HttpMessageReaderFactory() 
    {
    	
    }

    @Override
    public MessageReader createMessageReader() 
    {
        return new HttpMessageReader();
    }
}