package org.kaipan.jserver.socket.server;

import java.lang.reflect.Constructor;

import org.kaipan.jserver.socket.core.Server;

/**
 * server factory
 *     create wanted server for u
 * 
 * @author will<pan.kai@icloud.com>
 */
public class ServerFactory
{
	public static Server create(String name, String path) 
	{
		try {
			Class<?> ServerClass = Class.forName(name);
			
			Class<?>[] classes = new Class[] {
				String.class
			};
			
			Constructor<?> constructor = ServerClass.getConstructor(classes);
			
			Object[] arguments = new Object[] {
				path
			};
			
			return (Server) constructor.newInstance(arguments);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Unable to instantiate " + name);
		}
	}
}
