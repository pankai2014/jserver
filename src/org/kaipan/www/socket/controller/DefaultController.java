package org.kaipan.www.socket.controller;

import org.kaipan.www.socket.protocol.http.HttpRequest;
import org.kaipan.www.socket.protocol.http.HttpResponse;

public class DefaultController implements IController
{	
	public String run(HttpRequest request, HttpResponse response) 
	{
		return "Welcome, any quetions please contact with will<pan.kai@icloud.com>";
	}
}
