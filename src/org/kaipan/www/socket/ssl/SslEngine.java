package org.kaipan.www.socket.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;

import org.kaipan.www.socket.core.Socket;
import org.kaipan.www.socket.log.Logger;

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
        Logger.write("about to do handshake...", Logger.INFO);
        
        SSLEngineResult result;
        
        HandshakeStatus handshakeStatus = sslEngine.getHandshakeStatus();
        
        while ( handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED 
                && handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING ) {
            switch ( handshakeStatus ) {
                case NEED_UNWRAP:
                    Logger.write("NEED_UNWRAP", Logger.INFO);
                    
                    try {
                        if ( socket.read(peerNetData) < 0 ) {
                            if ( sslEngine.isInboundDone() && sslEngine.isOutboundDone() ) {
                                return false;
                            }
                            
                            try {
                                sslEngine.closeInbound();
                            } 
                            catch (SSLException e) {
                                Logger.write(e.getMessage(), Logger.ERROR);
                            }
                            
                            sslEngine.closeOutbound();
                            
                            handshakeStatus = sslEngine.getHandshakeStatus();
                            break;
                        }
                    } 
                    catch (IOException e) {
                        Logger.write(e.getMessage(), Logger.ERROR);
                    }
                    
                    peerNetData.flip();
                    
                    try {
                        result = sslEngine.unwrap(peerNetData, peerAppData);
                        
                        peerNetData.compact();
                        handshakeStatus = result.getHandshakeStatus();
                    } 
                    catch (SSLException e) {
                    	Logger.write(e.getMessage(), Logger.ERROR);
                    	
                        sslEngine.closeOutbound();
                        handshakeStatus = sslEngine.getHandshakeStatus();
                        
                        break;
                    }
                    
                    switch ( result.getStatus() ) {
                        case OK:
                            Logger.write("OK", Logger.INFO);
                            break;
                        case BUFFER_OVERFLOW:
                        	Logger.write("BUFFER_OVERFLOW", Logger.INFO);
                        	
                            /**
                             * will occur when peerAppData's capacity is smaller than the data derived from peerNetData's unwrap.
                             */
                            peerAppData = Ssl.enlargeApplicationBuffer(sslEngine, peerAppData);
                            
                            break;
                        case BUFFER_UNDERFLOW:
                        	Logger.write("BUFFER_UNDERFLOW", Logger.INFO);
                        	
                            /**
                             * will occur either when no data was read from the peer 
                             *     or when the peerNetData buffer was too small to hold all peer's data.
                             */
                            peerNetData = Ssl.handleBufferUnderflow(sslEngine, peerNetData);
                            
                            break;
                        case CLOSED:
                        	Logger.write("CLOSED", Logger.INFO);
                        	
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
                    Logger.write("NEED_WRAP", Logger.INFO);
                    
                    myNetData.clear();
                    try {
                        result = sslEngine.wrap(myAppData, myNetData);
                        handshakeStatus = result.getHandshakeStatus();
                    } 
                    catch (SSLException e) {
                    	Logger.write(e.getMessage(), Logger.ERROR);
                        
                        sslEngine.closeOutbound();
                        handshakeStatus = sslEngine.getHandshakeStatus();
                        
                        break;
                    }
                    
                    switch ( result.getStatus() ) {
                        case OK:
                            Logger.write("OK", Logger.INFO);
                            
                            myNetData.flip();
                            while ( myNetData.hasRemaining() ) {
                                try {
                                    socket.write(myNetData);
                                } 
                                catch (IOException e) {
                                   Logger.write(e.getMessage());
                                }
                            }
                            
                            break;
                        case BUFFER_OVERFLOW:
                            Logger.write("BUFFER_OVERFLOW", Logger.INFO);
                        	
                            myNetData = Ssl.enlargePacketBuffer(sslEngine, myNetData);
                            break;
                        case BUFFER_UNDERFLOW:
                        	Logger.write("BUFFER_UNDERFLOW");
                        	
                            try {
                                throw new SSLException("Buffer underflow occured after a wrap. i don't think we should ever get here");
                            } 
                            catch (SSLException e) {
                                Logger.write(e.getMessage(), Logger.ERROR);
                            }
                            
                            break;
                        case CLOSED:
                            Logger.write("CLOSED", Logger.INFO);
                            
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
                                Logger.write("failed to send server's close message due to socket channel's failure", Logger.ERROR);
                                
                                handshakeStatus = sslEngine.getHandshakeStatus();
                            }
                            
                            break;
                        default:
                            throw new IllegalStateException("Invalid ssl status: " + result.getStatus());
                    }
                        
                    break;
                case NEED_TASK:
                    Logger.write("NEED_TASK", Logger.INFO);
                    
                    Runnable task;
                    while ( (task = sslEngine.getDelegatedTask()) != null ) {
                        task.run();
                    }
                    
                    handshakeStatus = sslEngine.getHandshakeStatus();
                    
                    break;
                case FINISHED:
                    Logger.write("FINISHED", Logger.INFO);
                    
                    break;
                case NOT_HANDSHAKING:
                    Logger.write("NOT_HANDSHAKING", Logger.INFO);
                    
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
        	Logger.write(e.getMessage());
        }
        
        if ( socket.endOfStreamReached == true ) {
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
               Logger.write(e.getMessage(), Logger.ERROR);
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
								Logger.write(e.getMessage());
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
    			Logger.write(e.getMessage(), Logger.ERROR);
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
			Logger.write("this engine was forced to close inbound, without having received the proper SSL/TLS close notification message from the peer,"
					+ " due to end of stream", Logger.ERROR);
		}
        
        closeConnection(socket);
    }
    
    public SSLEngine getSslEngine() 
    {
        return sslEngine;
    }
}
