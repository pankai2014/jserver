package org.kaipan.www.socket.controller;

import org.kaipan.www.socket.protocol.http.HttpRequest;

public interface IController
{
	public String run(HttpRequest request);
}
