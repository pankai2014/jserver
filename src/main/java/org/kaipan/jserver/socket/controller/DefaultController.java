package org.kaipan.jserver.socket.controller;

import org.kaipan.jserver.socket.protocol.http.HttpRequest;
import org.kaipan.jserver.socket.protocol.http.HttpResponse;

public class DefaultController implements Controller
{	
	public String run(HttpRequest request, HttpResponse response) 
	{
		return "Welcome, any quetions please contact with will<pan.kai@icloud.com>";
	}
}
