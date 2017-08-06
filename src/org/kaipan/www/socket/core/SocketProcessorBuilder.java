package org.kaipan.www.socket.core;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.kaipan.www.socket.task.ITask;
import org.kaipan.www.socket.task.ITaskFactory;
import org.kaipan.www.socket.worker.MessageWorker;

public class SocketProcessorBuilder
{
	private Config config;
	
	private Queue<Socket>  socketQueue;
	private Queue<Message> outboundMessageQueue;
	
	private MessageWorker messageWorker;
	
	private Map<Long, Socket> socketMap;
	
	private ByteBuffer readByteBuffer;
	private ByteBuffer writeByteBuffer;
	
	private IMessageReaderFactory messageReaderFactory;
	
	private Set<Socket> emptyToNonEmptySockets;
	private Set<Socket> nonEmptyToEmptySockets;
	
	private ExecutorService acceptThreadPool;
	
	private ITaskFactory taskFactory;
	
	public SocketProcessorBuilder setIConfig(Config config) 
	{
		this.config = config;
		
		return this;
	}
	
	public SocketProcessorBuilder setSocketQueue(Queue<Socket> socketQueue) 
	{
		this.socketQueue = socketQueue;
		
		return this;
	}
	
	public SocketProcessorBuilder setOutputMessageQueue(Queue<Message> outboundMessageQueue) 
	{
		this.outboundMessageQueue = outboundMessageQueue;
		
		return this;
	}
	
	public SocketProcessorBuilder setMessageWorker(MessageWorker messageWorker) 
	{
		this.messageWorker = messageWorker;
		
		return this;
	}
	
	public SocketProcessorBuilder setSocketMap(Map<Long, Socket> socketMap) 
	{
		this.socketMap = socketMap;
		
		return this;
	}
	
	public SocketProcessorBuilder setReadByteBuffer(ByteBuffer readByteBuffer) 
	{
		this.readByteBuffer = readByteBuffer;
		
		return this;
	}
	
	public SocketProcessorBuilder setWriteByteBuffer(ByteBuffer writeByteBuffer) 
	{
		this.writeByteBuffer = writeByteBuffer;
		
		return this;
	}

	public SocketProcessorBuilder setMessageReaderFactory(IMessageReaderFactory messageReaderFactory) 
	{
		this.messageReaderFactory = messageReaderFactory;
		
		return this;
	}
	
	public SocketProcessorBuilder setEmptyToNonEmptySockets(Set<Socket> emptyToNonEmptySockets) 
	{
		this.emptyToNonEmptySockets = emptyToNonEmptySockets;
		
		return this;
	}
	
	public SocketProcessorBuilder setNonEmptyToEmptySockets(Set<Socket> nonEmptyToEmptySockets) 
	{
		this.nonEmptyToEmptySockets = nonEmptyToEmptySockets;
		
		return this;
	}
	
	public SocketProcessorBuilder setAcceptThreadPool(ExecutorService acceptThreadPool) 
	{
		this.acceptThreadPool = acceptThreadPool;
		
		return this;
	}
	
	public SocketProcessorBuilder setTaskFactory(ITaskFactory taskFactory) 
	{
		this.taskFactory = taskFactory;
		
		return this;
	}
	
	public SocketProcessor build()
	{
		if ( socketQueue == null ) 			  socketQueue = new ArrayBlockingQueue<Socket>(1024);
		if ( outboundMessageQueue == null )   outboundMessageQueue = new LinkedBlockingQueue<Message>(10000);
		
		if ( messageWorker == null )		  messageWorker = new MessageWorker(2000, 100, 300, 6, 
				new ArrayBlockingQueue<Runnable>(10000), new LinkedBlockingQueue<ITask>(10000));
		
		if ( socketMap == null )			  socketMap = new HashMap<>();
		
		if ( readByteBuffer == null )		  readByteBuffer = ByteBuffer.allocate(1024 * 1024 * 4);
		if ( writeByteBuffer == null )		  writeByteBuffer = ByteBuffer.allocate(1024 * 1024 * 4);
		
		if ( emptyToNonEmptySockets == null ) emptyToNonEmptySockets = new HashSet<>();
		if ( nonEmptyToEmptySockets == null ) nonEmptyToEmptySockets = new HashSet<>();
		
		if ( acceptThreadPool == null )		  acceptThreadPool = Executors.newFixedThreadPool(5);
		
		return new SocketProcessor(
				config,
				socketQueue,
				outboundMessageQueue,
				messageWorker,
				socketMap,
				readByteBuffer,
				writeByteBuffer,
				messageReaderFactory,
				emptyToNonEmptySockets,
				nonEmptyToEmptySockets,
				acceptThreadPool,
				taskFactory
		);
	}
}
