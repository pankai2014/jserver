package org.kaipan.jserver.socket.protocol.https;

import org.kaipan.jserver.socket.core.MessageReader;
import org.kaipan.jserver.socket.core.MessageReaderFactory;

public class HttpsMessageReaderFactory implements MessageReaderFactory 
{

    public HttpsMessageReaderFactory() 
    {
    	
    }

    public MessageReader createMessageReader() 
    {
        return new HttpsMessageReader();
    }
}