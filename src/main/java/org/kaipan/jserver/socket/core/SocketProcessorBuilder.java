package org.kaipan.jserver.socket.core;

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

import org.kaipan.jserver.socket.router.Router;
import org.kaipan.jserver.socket.task.Task;
import org.kaipan.jserver.socket.task.TaskFactory;
import org.kaipan.jserver.socket.worker.MessageWorker;

public class SocketProcessorBuilder
{
	private Server server;
	
	private Queue<Socket>  socketQueue;
	private Queue<Message> outboundMessageQueue;
	
	private MessageWorker messageWorker;
	
	private Map<Long, Socket> socketMap;
	
	private ByteBuffer readByteBuffer;
	private ByteBuffer writeByteBuffer;
	
	private MessageReaderFactory messageReaderFactory;
	
	private Set<Socket> emptyToNonEmptySockets;
	private Set<Socket> nonEmptyToEmptySockets;
	
	private ExecutorService acceptThreadPool;
	
	private TaskFactory taskFactory;
	
	private Router router;
	
	public SocketProcessorBuilder setServer(Server server) 
	{
		this.server = server;
		
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

	public SocketProcessorBuilder setMessageReaderFactory(MessageReaderFactory messageReaderFactory) 
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
	
	public SocketProcessorBuilder setTaskFactory(TaskFactory taskFactory) 
	{
		this.taskFactory = taskFactory;
		
		return this;
	}
	
	public SocketProcessorBuilder setRouter(Router router) 
	{
		this.router = router;
		
		return this;
	}
	
	public SocketProcessor build()
	{
		if ( socketQueue == null ) 			  socketQueue = new ArrayBlockingQueue<Socket>(1024);
		if ( outboundMessageQueue == null )   outboundMessageQueue = new LinkedBlockingQueue<Message>(10000);
		
		if ( messageWorker == null )		  messageWorker = new MessageWorker(2000, 100, 300, 6, 
				new ArrayBlockingQueue<Runnable>(10000), new LinkedBlockingQueue<Task>(10000));
		
		if ( socketMap == null )			  socketMap = new HashMap<Long, Socket>();
		
		if ( readByteBuffer == null )		  readByteBuffer = ByteBuffer.allocate(1024 * 1024 * 4);
		if ( writeByteBuffer == null )		  writeByteBuffer = ByteBuffer.allocate(1024 * 1024 * 4);
		
		if ( emptyToNonEmptySockets == null ) emptyToNonEmptySockets = new HashSet<Socket>();
		if ( nonEmptyToEmptySockets == null ) nonEmptyToEmptySockets = new HashSet<Socket>();
		
		if ( acceptThreadPool == null )		  acceptThreadPool = Executors.newFixedThreadPool(5);
		
		return new SocketProcessor(
				server,
				router,
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
