package org.kaipan.www.socket.protocol.https;

import java.util.Properties;

import org.kaipan.www.socket.core.MessageBuffer;
import org.kaipan.www.socket.core.Server;
import org.kaipan.www.socket.core.SocketProcessor;
import org.kaipan.www.socket.protocol.http.HttpMessageProcessor;
import org.kaipan.www.socket.protocol.http.HttpMessageReaderFactory;
import org.kaipan.www.socket.util.Utils;

public class HttpSslServer extends Server
{
    public HttpSslServer(HttpSslConfig config)
    {
        super(config);

        createSocketProcessor();
    }
    
    public HttpSslConfig getConfig() 
    {
        return (HttpSslConfig)iconfig;
    }

    @Override
    protected void createSocketProcessor()
    {
        //this.processor = new SocketProcessor(getConfig());
        //this.processor.init(new HttpsMessageReaderFactory(), new MessageBuffer(), new MessageBuffer());
    	
    	this.socketProcessor = SocketProcessor.custom()
        		.setIConfig(getConfig())
        		.setMessageReaderFactory(new HttpMessageReaderFactory())
        		.build();
    }
    
    public static void main(String[] args) 
    {
    	HttpSslConfig config = new HttpSslConfig();
        
        String path = null;
        if ( args.length > 0 ) path = args[0];
        
        Properties property = null;
        if ( path == null ) {
            String jarHome = Utils.getJarHome(config);
            
            property = Utils.loadConfigFile(jarHome + "/https-server.properties");
        }
        else {
            property = Utils.loadConfigFile(path);
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
