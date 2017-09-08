package org.kaipan.www.socket.router;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.kaipan.www.socket.controller.Controller;
import org.kaipan.www.socket.protocol.http.HttpRequest;

public class DynamicRouter implements Router
{
	private Map<String, Class<? extends Controller>> mapping;
	
	public DynamicRouter() 
	{
		mapping = new HashMap<String, Class<? extends Controller>>();
	}
	
	@Override
	public void addMapping(String path, Class<? extends Controller> controller)
	{
		mapping.put(path, controller);
	}

	@Override
	public void removeMapping(String path)
	{
		mapping.remove(path);
	}

	@Override
	public Controller getController(HttpRequest request)
	{
		if ( mapping.containsKey(request.path) ) {
			Class<? extends Controller> Controller = mapping.get(request.path);
			
			if ( ! Controller.class.isAssignableFrom(Controller) ) {
				return null;
			}
		
			try {
				Class<?>[] classes = new Class[] {
				};
				
				Constructor<? extends Controller> constructor = Controller.getConstructor(classes);
				
				Object[] arguments = new Object[] {
				};
				
				return (Controller) constructor.newInstance(arguments);
			} 
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				return null;
			}
		}
		
		return null;
	}
}
