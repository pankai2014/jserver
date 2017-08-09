package org.kaipan.www.socket.controller;

import org.kaipan.www.socket.protocol.http.HttpRequest;

public class DefaultController implements IController
{	
	public String run(HttpRequest request) 
	{
		return "Welcome, any quetions please contact with will<pan.kai@icloud.com>";
	}
}
