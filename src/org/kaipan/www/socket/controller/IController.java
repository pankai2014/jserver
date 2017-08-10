package org.kaipan.www.socket.controller;

import org.kaipan.www.socket.protocol.http.HttpRequest;
import org.kaipan.www.socket.protocol.http.HttpResponse;

public interface IController
{
	public String run(HttpRequest request, HttpResponse response);
}
