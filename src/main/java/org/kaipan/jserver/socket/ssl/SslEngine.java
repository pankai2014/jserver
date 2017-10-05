package org.kaipan.jserver.socket.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;

import org.kaipan.jserver.socket.core.Socket;
import org.kaipan.jserver.socket.log.Logger;

/**
 * SslEngine class
 * 
 * @author will<pan.kai@icloud.com>
 */
public class SslEngine
{
    private SSLEngine  sslEngine   = null;
    
    public ByteBuffer myAppData   = null;
    public ByteBuffer myNetData   = null;
    public ByteBuffer peerNetData = null;
    public ByteBuffer peerAppData = null;
    
    public SslEngine() 
    {
        
    }
    
    public SslEngine(SSLEngine sslEngine) 
    {
        this.sslEngine  = sslEngine;
    }
    
    public void init(ByteBuffer peerAppData, ByteBuffer myAppData) 
    {
        SSLSession sslSession = sslEngine.getSession();
        
        /**
         * peerNetData ------> peerAppData
         * myNetData  <------- myAppData  
         */
        this.myAppData 	 = myAppData   == null ? ByteBuffer.allocate(sslSession.getApplicationBufferSize()) : myAppData;
        this.myNetData 	 = myNetData   == null ? ByteBuffer.allocate(sslSession.getPacketBufferSize()) 	    : myNetData;
        
        this.peerAppData = peerAppData == null ? ByteBuffer.allocate(sslSession.getApplicationBufferSize()) : peerAppData;
        this.peerNetData = peerNetData == null ? ByteBuffer.allocate(sslSession.getPacketBufferSize()) 	    : peerNetData;
        
        /**
         * No new connections can be created, but any existing connection remains valid until it is closed.
         *      variable dummySession invalid later?
         */
        sslSession.invalidate();
    }
    
    public boolean doHandShake(Socket socket) 
    {
        Logger.info("about to do handshake...");
        
        SSLEngineResult result;
        
        HandshakeStatus handshakeStatus = sslEngine.getHandshakeStatus();
        
        while ( handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED 
                && handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING ) {
            switch ( handshakeStatus ) {
                case NEED_UNWRAP:
                    Logger.info("NEED_UNWRAP");
                    
                    try {
                        if ( socket.read(peerNetData) < 0 ) {
                            if ( sslEngine.isInboundDone() && sslEngine.isOutboundDone() ) {
                                return false;
                            }
                            
                            try {
                                sslEngine.closeInbound();
                            } 
                            catch (SSLException e) {
                                Logger.error(e.getStackTrace());
                            }
                            
                            sslEngine.closeOutbound();
                            
                            handshakeStatus = sslEngine.getHandshakeStatus();
                            break;
                        }
                    } 
                    catch (IOException e) {
                        Logger.error(e.getStackTrace());
                    }
                    
                    peerNetData.flip();
                    
                    try {
                        result = sslEngine.unwrap(peerNetData, peerAppData);
                        
                        peerNetData.compact();
                        handshakeStatus = result.getHandshakeStatus();
                    } 
                    catch (SSLException e) {
                    	Logger.error(e.getStackTrace());
                    	
                        sslEngine.closeOutbound();
                        handshakeStatus = sslEngine.getHandshakeStatus();
                        
                        break;
                    }
                    
                    switch ( result.getStatus() ) {
                        case OK:
                            Logger.info("OK");
                            break;
                        case BUFFER_OVERFLOW:
                        	Logger.info("BUFFER_OVERFLOW");
                        	
                            /**
                             * will occur when peerAppData's capacity is smaller than the data derived from peerNetData's unwrap.
                             */
                            peerAppData = Ssl.enlargeApplicationBuffer(sslEngine, peerAppData);
                            
                            break;
                        case BUFFER_UNDERFLOW:
                        	Logger.info("BUFFER_UNDERFLOW");
                        	
                            /**
                             * will occur either when no data was read from the peer 
                             *     or when the peerNetData buffer was too small to hold all peer's data.
                             */
                            peerNetData = Ssl.handleBufferUnderflow(sslEngine, peerNetData);
                            
                            break;
                        case CLOSED:
                        	Logger.info("CLOSED");
                        	
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
                            throw new IllegalStateException("Invalid ssl status: " + result.getStatus());
                    }
                    
                    break;
                case NEED_WRAP:
                    Logger.info("NEED_WRAP");
                    
                    myNetData.clear();
                    try {
                        result = sslEngine.wrap(myAppData, myNetData);
                        handshakeStatus = result.getHandshakeStatus();
                    } 
                    catch (SSLException e) {
                    	Logger.error(e.getStackTrace());
                        
                        sslEngine.closeOutbound();
                        handshakeStatus = sslEngine.getHandshakeStatus();
                        
                        break;
                    }
                    
                    switch ( result.getStatus() ) {
                        case OK:
                            Logger.info("OK");
                            
                            myNetData.flip();
                            while ( myNetData.hasRemaining() ) {
                                try {
                                    socket.write(myNetData);
                                } 
                                catch (IOException e) {
                                   Logger.error(e.getStackTrace());
                                }
                            }
                            
                            break;
                        case BUFFER_OVERFLOW:
                            Logger.info("BUFFER_OVERFLOW");
                        	
                            myNetData = Ssl.enlargePacketBuffer(sslEngine, myNetData);
                            break;
                        case BUFFER_UNDERFLOW:
                        	Logger.info("BUFFER_UNDERFLOW");
                        	
                            try {
                                throw new SSLException("Buffer underflow occured after a wrap. i don't think we should ever get here");
                            } 
                            catch (SSLException e) {
                                Logger.error(e.getStackTrace());
                            }
                            
                            break;
                        case CLOSED:
                            Logger.info("CLOSED");
                            
                            try {
                                myNetData.flip();
                                while ( myNetData.hasRemaining() ) {
                                    socket.write(myNetData);
                                }
                                
                                /**
                                 * at this point the handshake status will probably be NEED_UNWRAP so we make sure that peerNetData is clear to read
                                 */
                                peerNetData.clear();
                            } 
                            catch (Exception e) {
                                Logger.error(e.getStackTrace());
                                
                                handshakeStatus = sslEngine.getHandshakeStatus();
                            }
                            
                            break;
                        default:
                            throw new IllegalStateException("Invalid ssl status: " + result.getStatus());
                    }
                        
                    break;
                case NEED_TASK:
                    Logger.info("NEED_TASK");
                    
                    Runnable task;
                    while ( (task = sslEngine.getDelegatedTask()) != null ) {
                        task.run();
                    }
                    
                    handshakeStatus = sslEngine.getHandshakeStatus();
                    
                    break;
                case FINISHED:
                    Logger.info("FINISHED");
                    
                    break;
                case NOT_HANDSHAKING:
                    Logger.info("NOT_HANDSHAKING");
                    
                    break;
                default:
                    throw new IllegalStateException("Invalid ssl status: " + handshakeStatus);
            }
        }
        
        return true;
    }
    
    public int read(Socket socket) 
    {
        peerNetData.clear();
        peerAppData.clear();
        
        int bytesRead = 0;
        
        try {
        	bytesRead = socket.read(peerNetData);
        } 
        catch (IOException e) {
        	Logger.error(e.getStackTrace());
        	
        	handleEndOfStream(socket);
        }
        
        peerNetData.flip();
        
        SSLEngineResult result;
        
        while ( peerNetData.hasRemaining() ) {
            try {
                result = sslEngine.unwrap(peerNetData, peerAppData);
                
                switch ( result.getStatus() ) {
                    case OK:
                        break;
                    case BUFFER_OVERFLOW:
                        peerAppData = Ssl.enlargeApplicationBuffer(sslEngine, peerAppData);
                        
                        break;
                    case BUFFER_UNDERFLOW:
                        peerNetData = Ssl.handleBufferUnderflow(sslEngine, peerNetData);
                        
                        break;
                    case CLOSED:
                        closeConnection(socket);
                        
                        break;
                    default:
                        throw new IllegalStateException("Invalid ssl status: " + result.getStatus());
                }
            } 
            catch (SSLException e) {
               Logger.error(e.getStackTrace());
            }
        }
        
        return bytesRead;
    }
    
    public int write(Socket socket) 
    {
    	int bytesWrite = myAppData.remaining();
    	
    	myNetData.clear();
    	
    	SSLEngineResult result;
    	
    	while ( myAppData.hasRemaining() ) {
    		try {
				result = sslEngine.wrap(myAppData, myNetData);
				
				switch ( result.getStatus() ) {
				 	case OK:
		                myNetData.flip();
		                while ( myNetData.hasRemaining() ) {
		                    try {
								socket.write(myNetData);
							} 
		                    catch (IOException e) {
								Logger.error(e.getStackTrace());
							}
		                }
		                
		                return bytesWrite;
		            case BUFFER_OVERFLOW:
		                myNetData = Ssl.enlargePacketBuffer(sslEngine, myNetData);
		                
		                break;
		            case BUFFER_UNDERFLOW:
		            	
		                throw new SSLException("Buffer underflow occured after a wrap. i don't think we should ever get here");
		            case CLOSED:
		                closeConnection(socket);
		                
		                break;
		            default:
		                throw new IllegalStateException("Invalid ssl status: " + result.getStatus());
				}
			} 
    		catch (SSLException e) {
    			Logger.error(e.getStackTrace());
			}
    	}
    	
    	return -1;
    }
    
    private void closeConnection(Socket socket) 
    {
        sslEngine.closeOutbound();
        
        doHandShake(socket);
    }
    
    public void handleEndOfStream(Socket socket) 
    {
        try {
			sslEngine.closeInbound();
		} 
        catch (SSLException e) {
			Logger.error(e.getStackTrace());
		}
        
        closeConnection(socket);
    }
    
    public SSLEngine getSslEngine() 
    {
        return sslEngine;
    }
}
