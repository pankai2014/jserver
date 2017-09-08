package org.kaipan.www.socket.protocol.websocket;

import org.kaipan.www.socket.core.MessageReader;
import org.kaipan.www.socket.core.MessageReaderFactory;

public class WsMessageReaderFactroy implements MessageReaderFactory
{
    public WsMessageReaderFactroy() 
    {
        
    }

    @Override
    public MessageReader createMessageReader() 
    {
        return new WsMessageReader();
    }
}
