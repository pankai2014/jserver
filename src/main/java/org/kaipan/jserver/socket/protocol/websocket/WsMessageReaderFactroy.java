package org.kaipan.jserver.socket.protocol.websocket;

import org.kaipan.jserver.socket.core.MessageReader;
import org.kaipan.jserver.socket.core.MessageReaderFactory;

public class WsMessageReaderFactroy implements MessageReaderFactory
{
    public WsMessageReaderFactroy() 
    {
        
    }

    public MessageReader createMessageReader() 
    {
        return new WsMessageReader();
    }
}
