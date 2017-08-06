package org.kaipan.www.socket.protocol.http;

import java.util.Properties;

import org.kaipan.www.socket.controller.ToutiaoController;
import org.kaipan.www.socket.core.Server;
import org.kaipan.www.socket.core.SocketProcessor;
import org.kaipan.www.socket.log.Logger;
import org.kaipan.www.socket.task.HttpMessageTask;
import org.kaipan.www.socket.task.MessageTaskFactory;
import org.kaipan.www.socket.util.Utils;

public class HttpServer extends Server
{
    public HttpServer(HttpConfig config)
    {
        super(config);

        createSocketProcessor();
    }
    
    public HttpConfig getConfig() 
    {
        return (HttpConfig)iconfig;
    }

    @Override
    protected void createSocketProcessor()
    {
        //this.processor = new SocketProcessor(getConfig());
        //this.processor.init(new HttpMessageReaderFactory(), new MessageBuffer(), new MessageBuffer());
        //this.processor.addControllerMap("/toutiao", new ToutiaoController());
    	
    	this.socketProcessor = SocketProcessor.custom()
    		.setIConfig(getConfig())
    		.setMessageReaderFactory(new HttpMessageReaderFactory())
    		.setTaskFactory(new MessageTaskFactory(HttpMessageTask.class))
    		.build();
    }
    
    public static void main(String[] args) 
    {
        HttpConfig config = new HttpConfig();
        
        String path = null;
        if ( args.length > 0 ) path = args[0];
        
        Properties property = null;
        if ( path == null ) {
            String jarHome = Utils.getJarHome(config);
            
            property = Utils.loadConfigFile(jarHome + "/http-server.properties");
        }
        else {
            property = Utils.loadConfigFile(path);
            if ( property == null ) {
                Logger.write("Usage: java -jar http-server-{version}.jar "
                        + "\"path to file http-server.properties\"");
                return;
            }
        }
        config.load(property);
        
        HttpServer server = new HttpServer(config);
        server.start();
    }
}
