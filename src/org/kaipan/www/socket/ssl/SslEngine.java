package org.kaipan.www.socket.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;

import org.kaipan.www.socket.core.Log;
import org.kaipan.www.socket.core.Socket;

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
        
        init(sslEngine);
    }
    
    private void init(SSLEngine sslEngine) 
    {
        SSLSession sslSession = sslEngine.getSession();
        
        /**
         * peerNetData ------> peerAppData
         * myNetData  <------- myAppData  
         */
        myAppData 	= myAppData   == null ? ByteBuffer.allocate(sslSession.getApplicationBufferSize()) : myAppData;
        myNetData 	= myNetData   == null ? ByteBuffer.allocate(sslSession.getPacketBufferSize()) 	   : myNetData;
        
        peerAppData = peerAppData == null ? ByteBuffer.allocate(sslSession.getApplicationBufferSize()) : peerAppData;
        peerNetData = peerNetData == null ? ByteBuffer.allocate(sslSession.getPacketBufferSize()) 	   : peerNetData;
        
        /**
         * No new connections can be created, but any existing connection remains valid until it is closed.
         *      variable dummySession invalid later?
         */
        sslSession.invalidate();
    }
    
    public boolean doHandShake(Socket socket) 
    {
        Log.write("about to do handshake...");
        
        SSLEngineResult result;
        
        //SocketChannel socketChannel     = socket.getSocketChannel();
        HandshakeStatus handshakeStatus = sslEngine.getHandshakeStatus();
        
        while ( handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED 
                && handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING ) {
            switch ( handshakeStatus ) {
                case NEED_UNWRAP:
                    Log.write("NEED_UNWRAP");
                    
                    try {
                        if ( socket.read(peerNetData) < 0 ) {
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
                            peerAppData = Ssl.enlargeApplicationBuffer(sslEngine, peerAppData);
                            
                            break;
                        case BUFFER_UNDERFLOW:
                            Log.write("BUFFER_UNDERFLOW");
                            // will occur either when no data was read from the peer or when the peerNetData buffer was too small to hold all peer's data.
                            peerNetData = Ssl.handleBufferUnderflow(sslEngine, peerNetData);
                            
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
                                    socket.write(myNetData);
                                } 
                                catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                            
                            break;
                        case BUFFER_OVERFLOW:
                            Log.write("BUFFER_OVERFLOW");
                            myNetData = Ssl.enlargePacketBuffer(sslEngine, myNetData);
                            
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
                                    socket.write(myNetData);
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
    
    public int read(Socket socket) 
    {
        peerNetData.clear();
        peerAppData.clear();
        
        int bytesRead = 0;
        
        try {
        	bytesRead = socket.read(peerNetData);
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
                    	//System.out.println(new String(peerAppData.array(), 0, peerAppData.position()));
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
                        throw new IllegalStateException("invalid ssl status: " + result.getStatus());
                }
            } 
            catch (SSLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		                }
		                
		                return bytesWrite;
		            case BUFFER_OVERFLOW:
		                myNetData = Ssl.enlargePacketBuffer(sslEngine, myNetData);
		                
		                break;
		            case BUFFER_UNDERFLOW:
		            	
		                throw new SSLException("buffer underflow occured after a wrap. i don't think we should ever get here");
		            case CLOSED:
		                closeConnection(socket);
		                
		                break;
		            default:
		                throw new IllegalStateException("invalid ssl status: " + result.getStatus());
				}
			} 
    		catch (SSLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
        	//e.printStackTrace();
        	
			Log.write("this engine was forced to close inbound, without having received the proper SSL/TLS close notification message from the peer, due to end of stream");
		}
        
        closeConnection(socket);
    }
    
    public SSLEngine getSslEngine() 
    {
        return sslEngine;
    }
}
