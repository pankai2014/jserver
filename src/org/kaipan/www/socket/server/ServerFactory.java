package org.kaipan.www.socket.server;

import java.lang.reflect.Constructor;

import org.kaipan.www.socket.core.Server;

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
			throw new IllegalArgumentException("Unable to instantiate " + name);
		}
	}
	
	public static void main(String[] _args)
    {
		String[] args = {"WebsocketServer", null};
		
		String name = "WebsocketServer";
		String path = null;
		
		switch ( args.length ) {
			case 1:
				name = args[0];
				break;
			case 2:
				name = args[0];
				path = args[1];
				break;
			default:
				throw new IllegalArgumentException("Invalid operating parameters");
		}
		
		String[] parts = {ServerFactory.class.getPackage().getName(), ".", name};
		
		Server server = ServerFactory.create(String.join("", parts), path);
        server.start();
    }
}
