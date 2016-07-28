package org.kaipan.www.socket.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MessageWriter
{
	private List<Message> writeQueue  = new ArrayList<>();
	private Message messageInProgress = null;
	private int		bytesWritten	  = 0;
	
	public MessageWriter() 
	{
		
	}
	
	public void enqueue(Message message) 
	{
		if ( this.messageInProgress == null ) {
			this.messageInProgress = message;
		}
		else {
			this.writeQueue.add(message);
		}
	}
	
	public void write(Socket socket, ByteBuffer byteBuffer) throws IOException 
	{
        byteBuffer.put(this.messageInProgress.sharedArray, this.messageInProgress.offset + this.bytesWritten, this.messageInProgress.length - this.bytesWritten);
        byteBuffer.flip();

        this.bytesWritten += socket.write(byteBuffer);
        byteBuffer.clear();

        // byteBuffer.length(1024 * 1024 KB = 1MB) more than the (this.messageInProgress.length - this.bytesWritten)
        if ( bytesWritten >= this.messageInProgress.length ) {
            if ( this.writeQueue.size() > 0 ) {
                this.messageInProgress = this.writeQueue.remove(0);
            } 
            else {
                this.messageInProgress = null;
                //todo unregister from selector
            }
        }
        else {
        	// maybe byteBuffer.length less than (this.messageInProgress.length - this.bytesWritten)
        }
    }

    public boolean isEmpty() 
    {
        return this.writeQueue.isEmpty() && this.messageInProgress == null;
    }
}
