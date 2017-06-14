package org.kaipan.www.socket.websocket;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.kaipan.www.socket.core.IMessageReader;
import org.kaipan.www.socket.core.Message;
import org.kaipan.www.socket.core.MessageBuffer;
import org.kaipan.www.socket.core.Socket;

public class WsMessageReader implements IMessageReader
{
    private MessageBuffer    messageBuffer = null;
    private List<Message> completeMessages = new ArrayList<Message>();
    private Message            nextMessage = null;

    @Override
    public void initialize(MessageBuffer readMessageBuffer)
    {
        this.messageBuffer = readMessageBuffer;
    }

    @Override
    public boolean read(Socket socket, ByteBuffer byteBuffer)
    {
    	if ( nextMessage == null ) {
    		this.nextMessage = messageBuffer.getMessage();
    	}
    	
        return false;
    }

    @Override
    public List<Message> getMessages()
    {
        return completeMessages;
    }
}
