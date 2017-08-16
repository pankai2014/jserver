package org.kaipan.www.socket.core;

public class MessageBuffer
{
    public static int KB = 1024;
    public static int MB = 1024 * KB;

    private static final int CAPACITY_SMALL  =   4  * KB;
    private static final int CAPACITY_MEDIUM = 128  * KB;
    private static final int CAPACITY_LARGE  = 1024 * KB;
    private static final int CAPACITY_HUGE   =   4  * MB;

    //package scope (default) - so they can be accessed from unit tests.
    byte[]  smallMessageBuffer  = new byte[2048 *   4 * KB];   //2048 *   4KB messages =  8MB.
    byte[]  mediumMessageBuffer = new byte[256  * 128 * KB];   // 256 * 128KB messages = 32MB.
    byte[]  largeMessageBuffer  = new byte[32   *   1 * MB];   //  32 *   1MB messages = 32MB.
    byte[]  hugeMessageBuffer   = new byte[4    *   4 * MB];   //   4 *   4MB messages = 16MB. 

    QueueIntFlip smallMessageBufferFreeBlocks  = new QueueIntFlip(2048); // 2048 free sections
    QueueIntFlip mediumMessageBufferFreeBlocks = new QueueIntFlip(256);  // 256  free sections
    QueueIntFlip largeMessageBufferFreeBlocks  = new QueueIntFlip(32);   // 32   free sections
    QueueIntFlip hugeMessageBufferFreeBlocks   = new QueueIntFlip(4);    // 4    free sections

    //todo make all message buffer capacities and block sizes configurable
    //todo calculate free block queue sizes based on capacity and block size of buffers.

    public MessageBuffer() 
    {
        //add all free sections to all free section queues.
        for ( int i = 0; i < smallMessageBuffer.length; i += CAPACITY_SMALL ) {
            this.smallMessageBufferFreeBlocks.put(i);
        }
        
        for ( int i = 0; i < mediumMessageBuffer.length; i += CAPACITY_MEDIUM ) {
            this.mediumMessageBufferFreeBlocks.put(i);
        }
        
        for ( int i = 0; i < largeMessageBuffer.length; i += CAPACITY_LARGE ) {
            this.largeMessageBufferFreeBlocks.put(i);
        }
        
        for ( int i = 0; i < hugeMessageBuffer.length; i += CAPACITY_HUGE ) {
            this.hugeMessageBufferFreeBlocks.put(i);
        }
    }

    public synchronized Message getMessage() 
    {
        int nextFreeSmallBlock = this.smallMessageBufferFreeBlocks.take();

        if ( nextFreeSmallBlock == -1 ) return null;

        Message message = new Message(this); //todo get from Message pool - caps memory usage.

        message.sharedArray = this.smallMessageBuffer;
        message.capacity    = CAPACITY_SMALL;
        message.offset      = nextFreeSmallBlock;
        message.length      = 0;

        return message;
    }

    public boolean expandMessage(Message message)
    {
        if ( message.capacity == CAPACITY_SMALL) {
            return moveMessage(message, this.smallMessageBufferFreeBlocks, this.mediumMessageBufferFreeBlocks, this.mediumMessageBuffer, CAPACITY_MEDIUM);
        } 
        else if ( message.capacity == CAPACITY_MEDIUM ) {
            return moveMessage(message, this.mediumMessageBufferFreeBlocks, this.largeMessageBufferFreeBlocks, this.largeMessageBuffer, CAPACITY_LARGE);
        }
        else if ( message.capacity == CAPACITY_LARGE ) {
            return moveMessage(message, this.largeMessageBufferFreeBlocks, this.hugeMessageBufferFreeBlocks, this.hugeMessageBuffer, CAPACITY_HUGE);
        } 
        else {
            return false;
        }
    }

    private synchronized boolean moveMessage(Message message, QueueIntFlip srcBlockQueue, QueueIntFlip destBlockQueue, byte[] dest, int newCapacity) 
    {
        int nextFreeBlock = destBlockQueue.take();
        if ( nextFreeBlock == -1 ) return false;

        System.arraycopy(message.sharedArray, message.offset, dest, nextFreeBlock, message.length);

        //srcBlockQueue.put(message.offset); //free smaller block after copy

        message.sharedArray = dest;
        message.offset      = nextFreeBlock;
        message.capacity    = newCapacity;
        
        return true;
    }
}
