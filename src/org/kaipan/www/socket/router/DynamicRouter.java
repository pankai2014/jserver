package org.kaipan.www.socket.router;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.kaipan.www.socket.controller.IController;
import org.kaipan.www.socket.protocol.http.HttpRequest;

public class DynamicRouter implements IRouter
{
	private Map<String, Class<? extends IController>> mapping;
	
	public DynamicRouter() 
	{
		mapping = new HashMap<String, Class<? extends IController>>();
	}
	
	@Override
	public void addMapping(String path, Class<? extends IController> controller)
	{
		mapping.put(path, controller);
	}

	@Override
	public void removeMapping(String path)
	{
		mapping.remove(path);
	}

	@Override
	public IController getController(HttpRequest request)
	{
		if ( mapping.containsKey(request.path) ) {
			Class<? extends IController> Controller = mapping.get(request.path);
			
			if ( ! IController.class.isAssignableFrom(Controller) ) {
				return null;
			}
		
			try {
				Class<?>[] classes = new Class[] {
				};
				
				Constructor<? extends IController> constructor = Controller.getConstructor(classes);
				
				Object[] arguments = new Object[] {
				};
				
				return (IController) constructor.newInstance(arguments);
			} 
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				return null;
			}
		}
		
		return null;
	}
}
