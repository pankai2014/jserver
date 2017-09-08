package org.kaipan.www.socket.router;

import org.kaipan.www.socket.controller.Controller;
import org.kaipan.www.socket.protocol.http.HttpRequest;

public interface Router
{
	public void addMapping(String path, Class<? extends Controller> controller);
	
	public void removeMapping(String path);
	
	public Controller getController(HttpRequest request);
}
