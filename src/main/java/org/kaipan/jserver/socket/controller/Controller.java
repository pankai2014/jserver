package org.kaipan.jserver.socket.controller;

import org.kaipan.jserver.socket.protocol.http.HttpRequest;
import org.kaipan.jserver.socket.protocol.http.HttpResponse;

public interface Controller
{
	public String run(HttpRequest request, HttpResponse response);
}
