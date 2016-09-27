package org.kaipan.www.socket.ssl;

public interface SslConfig
{
    public boolean sslMode();
    
    public String sslProtocol();
    
    public String sslServerCertsFile();
    
    public String sslTrustsCertsFile();
    
    public String sslKeystorePassword();
    
    public String sslKeyPassword();
}
