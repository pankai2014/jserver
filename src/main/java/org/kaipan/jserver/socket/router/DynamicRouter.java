package org.kaipan.jserver.socket.router;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.kaipan.jserver.socket.controller.Controller;
import org.kaipan.jserver.socket.log.Logger;
import org.kaipan.jserver.socket.protocol.http.HttpRequest;

public class DynamicRouter implements Router
{
	private Map<String, Class<? extends Controller>> mapping;
	
	public DynamicRouter() 
	{
		mapping = new HashMap<String, Class<? extends Controller>>();
	}
	
	public void addMapping(String path, Class<? extends Controller> controller)
	{
		mapping.put(path, controller);
	}

	public void removeMapping(String path)
	{
		mapping.remove(path);
	}

	public Controller getController(HttpRequest request)
	{
		if ( mapping.containsKey(request.path) ) {
			Class<? extends Controller> Controller = mapping.get(request.path);
			
			if ( ! Controller.class.isAssignableFrom(Controller) ) {
				return null;
			}
		
			Class<?>[] classes = new Class[] {
			};
			
			Constructor<? extends Controller> constructor;
			try {
				constructor = Controller.getConstructor(classes);
				
				Object[] arguments = new Object[] {
				};
				
				return (Controller) constructor.newInstance(arguments);
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
		}
		
		return null;
	}
}
