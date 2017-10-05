package org.kaipan.jserver.socket.protocol.https;

import java.util.Properties;

import org.kaipan.jserver.socket.protocol.http.HttpConfig;
import org.kaipan.jserver.socket.ssl.SslConfig;

public class HttpsConfig extends HttpConfig implements SslConfig
{
    private boolean ssl_mode_open = true;
	private String	ssl_protocol  = "TLSv1.2";
	
	private String  ssl_server_certs_file;
	private String  ssl_trusts_certs_file;
	
	private String  ssl_key_store_password;
	private String  ssl_key_password;
	
    public HttpsConfig() 
    {
        
    }
    
    public HttpsConfig(Properties property) 
    {
        load(property);
    }
    
    @Override
	public void load(Properties property) 
    {
        super.load(property);
        
        if ( property.getProperty("server.ssl_mode_open") != null ) {
            this.ssl_mode_open = Boolean.valueOf(property.getProperty("server.ssl_mode_open")).booleanValue();
        }
        
        if ( property.getProperty("server.ssl_protocol") != null ) {
            this.ssl_protocol = property.getProperty("server.ssl_protocol");
        }
        
        if ( property.getProperty("server.ssl_server_certs_file") != null ) {
            this.ssl_server_certs_file = property.getProperty("server.ssl_server_certs_file");
        }
        
        if ( property.getProperty("server.ssl_trusts_certs_file") != null ) {
            this.ssl_trusts_certs_file = property.getProperty("server.ssl_trusts_certs_file");
        }
        
        if ( property.getProperty("server.ssl_key_store_password") != null ) {
            this.ssl_key_store_password = property.getProperty("server.ssl_key_store_password");
        }
        
        if ( property.getProperty("server.ssl_key_password") != null ) {
            this.ssl_key_password = property.getProperty("server.ssl_key_password");
        }
    }
    
    @Override
	public boolean sslMode() 
    {
    	return ssl_mode_open;
    }
    
    @Override
	public String sslProtocol() 
    {
    	return ssl_protocol;
    }
    
	public String sslServerCertsFile() 
    {
        return ssl_server_certs_file;
    }
    
	public String sslTrustsCertsFile() 
    {
        return ssl_trusts_certs_file;
    }
    
	public String sslKeystorePassword() 
    {
        return ssl_key_store_password;
    }
    
	public String sslKeyPassword() 
    {
        return ssl_key_password;
    }
}
