package org.kaipan.www.socket.task;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.kaipan.www.socket.core.Message;
import org.kaipan.www.socket.core.Server;
import org.kaipan.www.socket.core.Socket;
import org.kaipan.www.socket.log.Logger;

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
	
	@Override
	public Task createTask(Server server, Socket socket, Message message)
	{
		Task Task = null;
		
		try {
			Class<?>[] classes = new Class[] {
				Server.class,
				Socket.class,
				Message.class
			};
			
			Constructor<? extends Task> constructor = TaskClass.getConstructor(classes);
			
			Object[] arguments = new Object[] {
				server,
				socket,
				message
			};
			
			Task = (Task) constructor.newInstance(arguments);
		} 
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			Logger.error(e.getStackTrace());
		}
		
		return Task;
	}
}
