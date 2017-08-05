package org.kaipan.www.socket.protocol.websocket;

import org.kaipan.www.socket.core.IMessageReader;
import org.kaipan.www.socket.core.IMessageReaderFactory;

public class WsMessageReaderFactroy implements IMessageReaderFactory
{
    public WsMessageReaderFactroy() 
    {
        
    }

    @Override
    public IMessageReader createMessageReader() 
    {
        return new WsMessageReader();
    }
}
