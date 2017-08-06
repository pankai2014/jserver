package org.kaipan.www.socket.worker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.kaipan.www.socket.task.ITask;

public class MessageWorker implements IWorker
{
	/**
	 * current running state of the worker 
	 */
	private volatile int state = IWorker.READY;
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
	private BlockingQueue<ITask> taskQueue = null;
	
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
			BlockingQueue<ITask> taskQueue ) 
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
	 * @see	IWorker#getLeftTask() 
	 */
	@Override
	public int getLeftTask() 
	{
		return taskQueue.size();
	}
	
	/**
	 * @see	IWorker#addTask(Runnable) 
	 */
	@Override
	public boolean addTask(ITask task) 
	{
		return taskQueue.offer(task);
	}

	/**
	 * @see	IWorker#getAvailableThread() 
	 */
	@Override
	public int getAvailableThread() 
	{
		if ( threadPool == null ) return 0; 
		return threadPool.getMaximumPoolSize() - threadPool.getActiveCount();
	}

	/**
	 * @see	IWorker#getWorkingThread() 
	 */
	@Override
	public int getWorkingThread() 
	{
		return threadPool.getActiveCount();
	}

	/**
	 * @see	IWorker#couldHoldMore() 
	 */
	@Override
	public boolean couldHoldMore() 
	{
		int leftThread = threadPool.getMaximumPoolSize() - threadPool.getActiveCount();
		return ( leftThread > 0 );
	}

	/**
	 * @see	IWorker#process(String) 
	 */
	@Override
	public boolean process( Runnable task ) 
	{
		if ( couldHoldMore() ) {
			threadPool.execute(task);
			return true;
		}
		
		return false;
	}
	
	/**
	 * @see	IWorker#start()
	 */
	@Override
	public void start()
	{
		consumerThread = new Thread(new Runnable() {
			@Override
			public void run() 
			{
				while ( true ) {
					//check and handler the stoped operation
					if ( state == IWorker.STOPED ) break;
					
					//check and handler the the pause operation
					if ( state == IWorker.PAUSED ) {
						synchronized ( LOCK ) {
							try {
								LOCK.wait();
							} catch (InterruptedException e) {e.printStackTrace();}
						}
					}
					
					try {
						//check if the worker could hold more task
						if ( ! couldHoldMore() ) {
							Thread.sleep(busySleepMsec);
							continue;
						}
						
						//take a url from the task queue and process it
						ITask task = taskQueue.take();
						process(task);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		//start the task consumer thread
		consumerThread.start();
	}

	@Override
	public int getState() 
	{
		return state;
	}

	@Override
	public void pause() 
	{
		if ( state == IWorker.PAUSED ) return;
		state = IWorker.PAUSED;
	}

	@Override
	public void resume() 
	{
		if ( state == IWorker.RUNNING ) return;
		synchronized ( LOCK ) {
			state = IWorker.RUNNING;
			LOCK.notify();
		}
	}

	/**
	 * @see	IWorker#shutdown() 
	 */
	@Override
	public void shutdown() 
	{
		state = IWorker.STOPED;
		taskQueue.clear();
		threadPool.shutdown();
		
		//interrupt the consumer thread
		if ( consumerThread != null ) {
			consumerThread.interrupt();
		}
	}
}
