package org.kaipan.www.socket.http;

import org.kaipan.www.socket.IMessageReader;
import org.kaipan.www.socket.IMessageReaderFactory;

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