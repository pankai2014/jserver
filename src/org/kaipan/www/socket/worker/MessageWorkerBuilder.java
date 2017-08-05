package org.kaipan.www.socket.worker;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.kaipan.www.socket.task.Task;

public class MessageWorkerBuilder
{
	/**
	 * busy sleep millsecond 
	 */
	private volatile int busySleepMsec = -1;
	
	/**
	 * core size of worker thread pool 
	 */
	private int coreThreadSize = -1;
	
	/**
	 * max size of the worker thread pool 
	 */
	private int maxThreadSize = -1;
	
	/**
	 * thread alive second of the worker thread pool 
	 */
	private int threadAliveSecond = -1;
	
	/**
	 * working queue of the thread pool of the worker 
	 */
	private BlockingQueue<Runnable> threadPoolQueue = null;
	
	/**
	 * task queue of the worker 
	 */
	private BlockingQueue<Task> taskQueue = null;

	public int getCoreThreadSize() 
	{
		return coreThreadSize;
	}

	public MessageWorkerBuilder setCoreThreadSize(int coreThreadSize) 
	{
		this.coreThreadSize = coreThreadSize;
		return this;
	}

	public int getMaxThreadSize() 
	{
		return maxThreadSize;
	}

	public MessageWorkerBuilder setMaxThreadSize(int maxThreadSize) 
	{
		this.maxThreadSize = maxThreadSize;
		return this;
	}

	public int getThreadAliveSecond() 
	{
		return threadAliveSecond;
	}

	public MessageWorkerBuilder setThreadAliveSecond(
			int threadAliveSecond) 
	{
		this.threadAliveSecond = threadAliveSecond;
		return this;
	}

	public BlockingQueue<Runnable> getThreadPoolQueue() 
	{
		return threadPoolQueue;
	}

	public MessageWorkerBuilder setThreadPoolQueue(
			BlockingQueue<Runnable> threadPoolQueue) 
	{
		this.threadPoolQueue = threadPoolQueue;
		return this;
	}

	public BlockingQueue<Task> getTaskQueue() 
	{
		return taskQueue;
	}

	public MessageWorkerBuilder setTaskQueue(BlockingQueue<Task> taskQueue) 
	{
		this.taskQueue = taskQueue;
		return this;
	}
	
	public int getBusySleepMsec() 
	{
		return busySleepMsec;
	}

	public MessageWorkerBuilder setBusySleepMsec(int busySleepMsec) 
	{
		this.busySleepMsec = busySleepMsec;
		return this;
	}

	/**
	 * build the SpiderWorker according to the setting above 
	 * 
	 * @return	SpiderWorker
	 */
	public MessageWorker build()
	{
		if ( busySleepMsec == -1 )		busySleepMsec = 2000;
		if ( coreThreadSize == -1 )		coreThreadSize = 100;
		if ( maxThreadSize == -1 )		maxThreadSize = 300;
		if ( threadAliveSecond == -1 )	threadAliveSecond = 6;
		
		if ( threadPoolQueue == null ) {
			threadPoolQueue = new ArrayBlockingQueue<Runnable>(10000);
		}
		
		if ( taskQueue == null ) {
			taskQueue = new LinkedBlockingQueue<Task>(10000);
		}
		
		//create and return a self defined SpiderWorker
		return new MessageWorker(
			busySleepMsec,
			coreThreadSize,
			maxThreadSize,
			threadAliveSecond,
			threadPoolQueue,
			taskQueue
		);
	}
}
