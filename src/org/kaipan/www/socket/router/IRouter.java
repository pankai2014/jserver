package org.kaipan.www.socket.router;

import org.kaipan.www.socket.controller.IController;
import org.kaipan.www.socket.protocol.http.HttpRequest;

public interface IRouter
{
	public void addMapping(String path, Class<? extends IController> controller);
	
	public void removeMapping(String path);
	
	public IController getController(HttpRequest request);
}
