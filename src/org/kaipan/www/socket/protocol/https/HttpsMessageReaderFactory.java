package org.kaipan.www.socket.protocol.https;

import org.kaipan.www.socket.core.MessageReader;
import org.kaipan.www.socket.core.MessageReaderFactory;

public class HttpsMessageReaderFactory implements MessageReaderFactory 
{

    public HttpsMessageReaderFactory() 
    {
    	
    }

    @Override
    public MessageReader createMessageReader() 
    {
        return new HttpsMessageReader();
    }
}