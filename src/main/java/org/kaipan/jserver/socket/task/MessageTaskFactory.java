package org.kaipan.jserver.socket.task;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.kaipan.jserver.socket.core.Message;
import org.kaipan.jserver.socket.core.Server;
import org.kaipan.jserver.socket.core.Socket;
import org.kaipan.jserver.socket.log.Logger;

public class MessageTaskFactory implements TaskFactory
{
	private Class<? extends Task> TaskClass;
	
	public MessageTaskFactory(Class<? extends Task> TaskClass) 
	{
		this.TaskClass = TaskClass;
	}
	
	public void setTaskClass(Class<? extends Task> TaskClass) 
	{
		this.TaskClass = TaskClass;
	}
	
	public Task createTask(Server server, Socket socket, Message message)
	{
		Task Task = null;
		
		Class<?>[] classes = new Class[] {
			Server.class,
			Socket.class,
			Message.class
		};
		
		try {
			Constructor<? extends Task> constructor = TaskClass.getConstructor(classes);
			
			Object[] arguments = new Object[] {
				server,
				socket,
				message
			};
			
			Task = (Task) constructor.newInstance(arguments);
		} 
		catch (NoSuchMethodException e) {
			Logger.error(e.getStackTrace());
		}
		catch (SecurityException e) {
			Logger.error(e.getStackTrace());
		} 
		catch (InstantiationException e) {
			Logger.error(e.getStackTrace());
		} 
		catch (IllegalAccessException e) {
			Logger.error(e.getStackTrace());
		} 
		catch (IllegalArgumentException e) {
			Logger.error(e.getStackTrace());
		} 
		catch (InvocationTargetException e) {
			Logger.error(e.getStackTrace());
		}
		
		return Task;
	}
}
