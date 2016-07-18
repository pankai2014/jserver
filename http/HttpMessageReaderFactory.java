package org.kaipan.www.sockets.http;

import org.kaipan.www.sockets.IMessageReader;
import org.kaipan.www.sockets.IMessageReaderFactory;

public class HttpMessageReaderFactory implements IMessageReaderFactory {

    public HttpMessageReaderFactory() 
    {
    	
    }

    @Override
    public IMessageReader createMessageReader() 
    {
        return new HttpMessageReader();
    }
}