package org.kaipan.jserver.socket.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

/**
 * Stream class applies to I/O non-blocking operation
 * 
 * @author will<pan.kai@icloud.com>
 */
public class Stream
{
	public static int read(SocketChannel socketChannel, ByteBuffer byteBuffer) throws IOException 
	{
        int bytesRead = socketChannel.read(byteBuffer);
        int totalBytesRead = bytesRead;

        while ( bytesRead > 0 ) {
            bytesRead = socketChannel.read(byteBuffer);
            totalBytesRead += bytesRead;
        }
        
        if ( bytesRead == -1 ) {
            throw new ClosedChannelException();
        }

        return totalBytesRead;
    }

	  
    public static int write(SocketChannel socketChannel, ByteBuffer byteBuffer) throws IOException 
    {
        int bytesWritten 	  = socketChannel.write(byteBuffer);
        int totalBytesWritten = bytesWritten; 

        /**
         * ByteBuffer method: remaining
         *    return limit(length) = opacity - position
         */
        while ( bytesWritten > 0 && byteBuffer.hasRemaining() ) {
            bytesWritten = socketChannel.write(byteBuffer);
            totalBytesWritten += bytesWritten;
        }

        return totalBytesWritten;
    }
}
