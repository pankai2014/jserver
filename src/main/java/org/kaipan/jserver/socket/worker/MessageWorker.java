package org.kaipan.jserver.socket.worker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.kaipan.jserver.socket.log.Logger;
import org.kaipan.jserver.socket.task.Task;

public class MessageWorker implements Worker
{
	/**
	 * current running state of the worker 
	 */
	private volatile int state = Worker.READY;
	private Object LOCK = new Object();
	
	/**
	 * busy sleep millsecond 
	 */
	private volatile int busySleepMsec = -1;
	
	/**
	 * thread pool 
	 */
	private ThreadPoolExecutor threadPool = null;
	
	/**
	 * task queue 
	 */
	private BlockingQueue<Task> taskQueue = null;
	
	/**
	 * consumer thread 
	 */
	private Thread consumerThread = null;
	
	/**
	 * create a worker builder so you can define the worker
	 * 
	 * @return	SpiderWorkerBuilder
	 */
	public static MessageWorkerBuilder custom()
	{
		return new MessageWorkerBuilder();
	}
	
	/**
	 * construct method
	 * 
	 * @param	coreThreadSize
	 * @param	maxThreadSize
	 * @param	threadAliveSec
	 * @param	threadPoolQueue
	 * @param	taskQueue
	 */
	public MessageWorker( 
			int busySleepMsec,
			int coreThreadSize,
			int maxThreadSize,
			int threadAliveSec,
			BlockingQueue<Runnable> threadPoolQueue,
			BlockingQueue<Task> taskQueue ) 
	{
		this.busySleepMsec = busySleepMsec;
		this.taskQueue = taskQueue;
		
		//create the thread pool
		threadPool = new ThreadPoolExecutor( 
				coreThreadSize, maxThreadSize, threadAliveSec, 
				TimeUnit.SECONDS, 
				threadPoolQueue, 
        		new ThreadPoolExecutor.DiscardOldestPolicy()
		);
	}

	/**
	 * @see	Worker#getLeftTask() 
	 */
	public int getLeftTask() 
	{
		return taskQueue.size();
	}
	
	/**
	 * @see	Worker#addTask(Runnable) 
	 */
	public boolean addTask(Task task) 
	{
		return taskQueue.offer(task);
	}

	/**
	 * @see	Worker#getAvailableThread() 
	 */
	public int getAvailableThread() 
	{
		if ( threadPool == null ) return 0; 
		return threadPool.getMaximumPoolSize() - threadPool.getActiveCount();
	}

	/**
	 * @see	Worker#getWorkingThread() 
	 */
	public int getWorkingThread() 
	{
		return threadPool.getActiveCount();
	}

	/**
	 * @see	Worker#couldHoldMore() 
	 */
	public boolean couldHoldMore() 
	{
		int leftThread = threadPool.getMaximumPoolSize() - threadPool.getActiveCount();
		return ( leftThread > 0 );
	}

	/**
	 * @see	Worker#process(String) 
	 */
	public boolean process( Runnable task ) 
	{
		if ( couldHoldMore() ) {
			threadPool.execute(task);
			return true;
		}
		
		return false;
	}
	
	/**
	 * @see	Worker#start()
	 */
	public void start()
	{
		consumerThread = new Thread(new Runnable() {
			public void run() 
			{
				while ( true ) {
					//check and handler the stoped operation
					if ( state == Worker.STOPED ) break;
					
					//check and handler the the pause operation
					if ( state == Worker.PAUSED ) {
						synchronized ( LOCK ) {
							try {
								LOCK.wait();
							} 
							catch (InterruptedException e) {
								Logger.error(e.getStackTrace());
							}
						}
					}
					
					try {
						//check if the worker could hold more task
						if ( ! couldHoldMore() ) {
							Thread.sleep(busySleepMsec);
							continue;
						}
						
						//take a url from the task queue and process it
						Task task = taskQueue.take();
						process(task);
					} 
					catch (InterruptedException e) {
						Logger.error(e.getStackTrace());
					}
				}
			}
		});
		
		//start the task consumer thread
		consumerThread.start();
	}

	public int getState() 
	{
		return state;
	}

	public void pause() 
	{
		if ( state == Worker.PAUSED ) return;
		state = Worker.PAUSED;
	}

	public void resume() 
	{
		if ( state == Worker.RUNNING ) return;
		synchronized ( LOCK ) {
			state = Worker.RUNNING;
			LOCK.notify();
		}
	}

	/**
	 * @see	Worker#shutdown() 
	 */
	public void shutdown() 
	{
		state = Worker.STOPED;
		taskQueue.clear();
		threadPool.shutdown();
		
		//interrupt the consumer thread
		if ( consumerThread != null ) {
			consumerThread.interrupt();
		}
	}
}
