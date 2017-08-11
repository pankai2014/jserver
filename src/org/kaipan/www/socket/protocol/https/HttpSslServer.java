package org.kaipan.www.socket.protocol.https;

import java.util.Properties;

import org.kaipan.www.socket.controller.DefaultController;
import org.kaipan.www.socket.core.Server;
import org.kaipan.www.socket.core.SocketProcessor;
import org.kaipan.www.socket.protocol.http.HttpMessageReaderFactory;
import org.kaipan.www.socket.router.DynamicRouter;
import org.kaipan.www.socket.task.HttpMessageTask;
import org.kaipan.www.socket.task.MessageTaskFactory;
import org.kaipan.www.socket.util.Util;

public class HttpSslServer extends Server
{
    public HttpSslServer(HttpSslConfig config)
    {
        super(config);

        createSocketProcessor();
    }
    
    public HttpSslConfig getConfig() 
    {
        return (HttpSslConfig) config;
    }

    @Override
    protected void createSocketProcessor()
    {
    	DynamicRouter router = new DynamicRouter();
    	router.addMapping("/default", DefaultController.class);
    	
    	this.socketProcessor = SocketProcessor.custom()
    		.setConfig(getConfig())
    		.setMessageReaderFactory(new HttpMessageReaderFactory())
    		.setTaskFactory(new MessageTaskFactory(HttpMessageTask.class))
    		.setRouter(router)
    		.build();
    }
    
    public static void main(String[] args) 
    {
    	HttpSslConfig config = new HttpSslConfig();
        
        String path = null;
        if ( args.length > 0 ) path = args[0];
        
        Properties property = null;
        if ( path == null ) {
            String jarHome = Util.getJarHome(config);
            
            property = Util.loadConfigFile(jarHome + "/https-server.properties");
        }
        else {
            property = Util.loadConfigFile(path);
            if ( property == null ) {
                System.out.println("Usage: java -jar https-server-{version}.jar "
                        + "\"path to file https-server.properties\"");
                return;
            }
        }
        config.load(property);
        
        HttpSslServer server = new HttpSslServer(config);
        server.start();
    }
}
