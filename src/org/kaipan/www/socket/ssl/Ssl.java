package org.kaipan.www.socket.ssl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
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
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.kaipan.www.socket.core.Log;

/**
 * Ssl class
 * 
 * @author will<pan.kai@icloud.com>
 */
public class Ssl 
{
    private String protocol = "TLSv1.2";
    
	private SSLContext context     = null;;
	
	private ByteBuffer myAppData   = null;
	private ByteBuffer myNetData   = null;
	private ByteBuffer peerAppData = null;
	private ByteBuffer peerNetData = null;
	
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
        
        SSLSession dummySession = context.createSSLEngine().getSession();
        
        myAppData   = ByteBuffer.allocate(dummySession.getApplicationBufferSize());
        myNetData   = ByteBuffer.allocate(dummySession.getPacketBufferSize());
        peerAppData = ByteBuffer.allocate(dummySession.getApplicationBufferSize());
        peerNetData = ByteBuffer.allocate(dummySession.getPacketBufferSize());
        
        /**
         * No new connections can be created, but any existing connection remains valid until it is closed.
         *      variable dummySession invalid later?
         */
        dummySession.invalidate();
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
	
	public SSLEngine createSslEngine() 
	{
	    SSLEngine sslEngine = context.createSSLEngine();
	    sslEngine.setUseClientMode(false);
	    
	    try {
            sslEngine.beginHandshake();
        } 
	    catch (SSLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	    
	    return sslEngine;
	}
	
	public boolean doHandShake(SocketChannel socketChannel, SSLEngine sslEngine) 
	{
	    Log.write("start to do handshake...");
	    
	    SSLEngineResult result;
	    HandshakeStatus handshakeStatus;
	    
	    /**
	     * peerNetData------>peerAppData
	     * myNetData <-------myAppData  
	     */
	    
	   //int appBufferSize = sslEngine.getSession().getApplicationBufferSize();
	    
	    //ByteBuffer myAppData   = ByteBuffer.allocate(appBufferSize);
	    //ByteBuffer peerAppData = ByteBuffer.allocate(appBufferSize);
	    
	    myAppData.clear();
	    peerAppData.clear();
	    
	    myNetData.clear();
	    peerNetData.clear();
	    
	    handshakeStatus = sslEngine.getHandshakeStatus();
	    
	    while ( handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED 
	            && handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING ) {
	        switch ( handshakeStatus ) {
	            case NEED_UNWRAP:
	                Log.write("NEED_UNWRAP");
	                
                    try {
                        if ( socketChannel.read(peerNetData) < 0 ) {
    	                    if ( sslEngine.isInboundDone() && sslEngine.isOutboundDone() ) {
    	                        return false;
    	                    }
    	                    
    	                    sslEngine.closeInbound();
    	                    sslEngine.closeOutbound();
    	                    
    	                    handshakeStatus = sslEngine.getHandshakeStatus();
    	                    
    	                    break;
    	                }
                    } 
                    catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                    peerNetData.flip();
	                
                    try {
                        result = sslEngine.unwrap(peerNetData, peerAppData);
                        
                        peerNetData.compact();
                        handshakeStatus = result.getHandshakeStatus();
                    } 
                    catch (SSLException e) {
                        e.printStackTrace();
                       //Log.write("a problem was encountered while processing the data that caused the SSLEngine to abort");
                        
                        sslEngine.closeOutbound();
                        handshakeStatus = sslEngine.getHandshakeStatus();
                        
                        break;
                    }
                    
                    switch ( result.getStatus() ) {
                        case OK:
                            Log.write("OK");
                            break;
                        case BUFFER_OVERFLOW:
                            Log.write("BUFFER_OVERFLOW");
                            // will occur when peerAppData's capacity is smaller than the data derived from peerNetData's unwrap.
                            peerAppData = enlargeApplicationBuffer(sslEngine, peerAppData);
                            
                            break;
                        case BUFFER_UNDERFLOW:
                            Log.write("BUFFER_UNDERFLOW");
                            // will occur either when no data was read from the peer or when the peerNetData buffer was too small to hold all peer's data.
                            peerNetData = handleBufferUnderflow(sslEngine, peerNetData);
                            
                            break;
                        case CLOSED:
                            Log.write("CLOSED");
                            // isInboundDone() is true
                            if ( sslEngine.isOutboundDone() ) {
                                return false;
                            } 
                            else {
                                sslEngine.closeOutbound();
                                handshakeStatus = sslEngine.getHandshakeStatus();
                                
                                break;
                            }
                        default:
                            throw new IllegalStateException("invalid ssl status: " + result.getStatus());
                    }
                    
	                break;
	            case NEED_WRAP:
	                Log.write("NEED_WRAP");
	                
                    myNetData.clear();
                    
                    try {
                        result = sslEngine.wrap(myAppData, myNetData);
                        handshakeStatus = result.getHandshakeStatus();
                    } 
                    catch (SSLException e) {
                        Log.write("a problem was encountered while processing the data that caused the SSLEngine to abort");
                        
                        sslEngine.closeOutbound();
                        handshakeStatus = sslEngine.getHandshakeStatus();
                        
                        break;
                    }
                    
                    switch ( result.getStatus() ) {
                        case OK:
                            Log.write("OK");
                            myNetData.flip();
                            while ( myNetData.hasRemaining() ) {
                                try {
                                    socketChannel.write(myNetData);
                                } 
                                catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                            
                            break;
                        case BUFFER_OVERFLOW:
                            Log.write("BUFFER_OVERFLOW");
                            myNetData = enlargePacketBuffer(sslEngine, myNetData);
                            
                            break;
                        case BUFFER_UNDERFLOW:
                            Log.write("BUFFER_UNDERFLOW");
                            try {
                                throw new SSLException("buffer underflow occured after a wrap. i don't think we should ever get here");
                            } 
                            catch (SSLException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                            
                            break;
                        case CLOSED:
                            Log.write("CLOSED");
                            try {
                                myNetData.flip();
                                while ( myNetData.hasRemaining() ) {
                                    socketChannel.write(myNetData);
                                }
                                
                                // At this point the handshake status will probably be NEED_UNWRAP so we make sure that peerNetData is clear to read
                                peerNetData.clear();
                            } 
                            catch (Exception e) {
                                Log.write("failed to send server's close message due to socket channel's failure");
                                handshakeStatus = sslEngine.getHandshakeStatus();
                            }
                            
                            break;
                        default:
                            throw new IllegalStateException("invalid ssl status: " + result.getStatus());
                    }
	                    
                    break;
                case NEED_TASK:
                    Log.write("NEED_TASK");
                    
                    Runnable task;
                    while ( (task = sslEngine.getDelegatedTask()) != null ) {
                        task.run();
                    }
                    
                    handshakeStatus = sslEngine.getHandshakeStatus();
                    
                    break;
                case FINISHED:
                    Log.write("FINISHED");
                    
                    break;
                case NOT_HANDSHAKING:
                    Log.write("NOT_HANDSHAKING");
                    
                    break;
                default:
                    throw new IllegalStateException("invalid ssl status: " + handshakeStatus);
	        }
	    }
	    
	    return true;
	}
	
	private static ByteBuffer enlargePacketBuffer(SSLEngine sslEngine, ByteBuffer buffer) 
    {
	    return enlargeBuffer(buffer, sslEngine.getSession().getPacketBufferSize());
    }
	
	private static ByteBuffer enlargeApplicationBuffer(SSLEngine sslEngine, ByteBuffer buffer) 
	{
	    return enlargeBuffer(buffer, sslEngine.getSession().getApplicationBufferSize());
	}
	
	private static ByteBuffer enlargeBuffer(ByteBuffer buffer, int sessionProposedCapacity) 
	{
        if ( sessionProposedCapacity > buffer.capacity() ) {
            buffer = ByteBuffer.allocate(sessionProposedCapacity);
        } 
        else {
            buffer = ByteBuffer.allocate(buffer.capacity() * 2);
        }
        
        return buffer;
    }
	
	private static ByteBuffer handleBufferUnderflow(SSLEngine sslEngine, ByteBuffer buffer) 
    {
	    if ( buffer.position() < buffer.limit() ) {
            return buffer;
        } 
	    else {
            ByteBuffer replaceBuffer = enlargePacketBuffer(sslEngine, buffer);
            buffer.flip();
            replaceBuffer.put(buffer);
            
            return replaceBuffer;
        }
    }
}
