package org.kaipan.jserver.socket.router;

import org.kaipan.jserver.socket.controller.Controller;
import org.kaipan.jserver.socket.protocol.http.HttpRequest;

public interface Router
{
	public void addMapping(String path, Class<? extends Controller> controller);
	
	public void removeMapping(String path);
	
	public Controller getController(HttpRequest request);
}
