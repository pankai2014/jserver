package org.kaipan.www.socket.test;

import org.kaipan.www.socket.core.Server;
import org.kaipan.www.socket.server.ServerFactory;

public class SFactory
{
	public static void main(String[] _args)
    {
		String[] args = {"DefaultHttpServer", null};
		
		String name = "DefaultHttpServer";
		String path = null;
		
		switch ( args.length ) {
			case 1:
				name = args[0];
				break;
			case 2:
				name = args[0];
				path = args[1];
				break;
			default:
				throw new IllegalArgumentException("Invalid operating parameters");
		}
		
		String[] parts = {ServerFactory.class.getPackage().getName(), ".", name};
		
		Server server = ServerFactory.create(String.join("", parts), path);
        server.start();
    }
}
