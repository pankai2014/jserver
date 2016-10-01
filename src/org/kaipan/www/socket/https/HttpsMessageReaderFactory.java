package org.kaipan.www.socket.https;

import org.kaipan.www.socket.core.IMessageReader;
import org.kaipan.www.socket.core.IMessageReaderFactory;

public class HttpsMessageReaderFactory implements IMessageReaderFactory 
{

    public HttpsMessageReaderFactory() 
    {
    	
    }

    @Override
    public IMessageReader createMessageReader() 
    {
        return new HttpsMessageReader();
    }
}