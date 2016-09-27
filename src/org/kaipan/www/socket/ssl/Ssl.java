package org.kaipan.www.socket.ssl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Ssl class
 * 
 * @author will<pan.kai@icloud.com>
 */
public class Ssl 
{
    private String protocol = "TLSv1.2";
    
	private SSLContext context;
	
	public Ssl() 
	{
		
	}
	
    public Ssl(String protocol) 
    {
        this.protocol = protocol;
    }
	
	public void init(String serverCertsFile, String trustedCertsFile, String keystorePassword, String keyPassword) 
	{
	    KeyManager[]   km = createKeyManagers(serverCertsFile, keystorePassword, keyPassword);
        TrustManager[] tm = createTrustManagers(trustedCertsFile, keystorePassword);
        
        SecureRandom random = new SecureRandom();
        
        try {
            context = SSLContext.getInstance(this.protocol);
            context.init(km, tm, random);
        } 
        catch (NoSuchAlgorithmException | KeyManagementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
	
	public void setProtocol(String protocol) 
	{
	    this.protocol = protocol;
	}
	
	private static KeyManager[] createKeyManagers(String filepath, String keystorePassword, String keyPassword) 
	{
	    try {
            KeyStore      keyStore = KeyStore.getInstance("JKS");
            InputStream keyStoreIS = new FileInputStream(filepath);
            
            keyStore.load(keyStoreIS, keystorePassword.toCharArray());
            
            /**
             * Default algorithm: "SunX509"
             *      select certificate to prove server identity
             */
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, keyPassword.toCharArray());
            return kmf.getKeyManagers();
	    } 
	    catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | UnrecoverableKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	    
	    return null;
	}
	
	private static TrustManager[] createTrustManagers(String filepath, String keystorePassword) 
	{
        try {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            
            InputStream trustStoreIS = new FileInputStream(filepath);
            trustStore.load(trustStoreIS, keystorePassword.toCharArray());
            
            /**
             * Default algorithm: "SunX509"
             *      determine Whether to trust client certificates
             */
            TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustFactory.init(trustStore);
            
            return trustFactory.getTrustManagers();
        } 
        catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       
        return null;
     }
}
