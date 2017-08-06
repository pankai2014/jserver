package org.kaipan.www.socket.worker;

import org.kaipan.www.socket.task.ITask;

public interface IWorker
{
	public static final int READY = 0;
	public static final int RUNNING = 1;
	public static final int PAUSED = 2;
	public static final int STOPED = 3;
	
	/**
	 * get the task number of the task queue
	 * 
	 * @param	int
	 */
	public int getLeftTask();
	
	/**
	 * add a new task to the task queue 
	 * 
	 * @param	task
	 */
	public boolean addTask( ITask task );
	
	/**
	 * get the available thread of the current worker
	 * 
	 * @return	int
	 */
	public int getAvailableThread();
	
	/**
	 * get the current working thread for the worker
	 * 
	 * @param	int
	 */
	public int getWorkingThread();
	
	/**
	 * wether could accept more task
	 * 
	 * @return boolean
	 */
	public boolean couldHoldMore();
	
	/**
	 * accept the specifield task
	 * 
	 * @param	task
	 * @return	boolean
	 */
	public boolean process( Runnable task );
	
	/**
	 * get the current working state 
	 */
	public int getState();
	
	/**
	 * start the worker 
	 */
	public void start();
	
	/**
	 * pause the worker 
	 */
	public void pause();
	
	/**
	 * resume the worker 
	 */
	public void resume();
	
	/**
	 * shutdown the worker 
	 */
	public void shutdown();
}
