package org.kaipan.www.socket.fastcgi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.kaipan.www.socket.core.Message;
import org.kaipan.www.socket.core.MessageBuffer;

/**
 * Factcgi client class
 * 
 * @author will<pan.kai@icloud.com>
 */
public class Client
{
    public final static int VERSION           = 1;
    
    public final static int BEGIN_REQUEST     = 1;
    public final static int ABORT_REQUEST     = 2;
    public final static int END_REQUEST       = 3;
    public final static int PARAMS            = 4;
    public final static int STDIN             = 5;
    public final static int STDOUT            = 6;
    public final static int STDERR            = 7;
    public final static int DATA              = 8;
    public final static int GET_VALUES        = 9;
    public final static int GET_VALUES_RESULT = 10;
    public final static int UNKNOWN           = 11;
    public final static int MAX               = UNKNOWN; 
   
    public final static int ROLE_RESPONDER    = 1;
    public final static int ROLE_AUTHORIZER   = 2;
    public final static int ROLE_FILTER       = 3;
    
    public final static int HEADER_LEN         = 8;
    
    private String host = null;
    
    private int port;
    private int connectTimeOut   = 5000;
    private int readWriteTimeOut = 5000;
    
    public byte[]	   readBytes	   = new byte[65535];	
    public ByteBuffer readBytesBuffer  = ByteBuffer.wrap(readBytes);
    public ByteBuffer writeBytesBuffer = ByteBuffer.allocate(65535);
    
    private Socket  client  = null;
    private Message message = null;
    
    private boolean keepAlive = false;
    
    public Client(String host, int port) 
    {
        this.host = host;
        this.port = port;
    }
    
    public void initialize(Message message) 
    {
        this.message = message;
    }
    
    public void connect() 
    {
        if ( client != null ) return;
        
        client = new Socket();
        
        SocketAddress address = new InetSocketAddress(host, port);
        try {
            client.connect(address, connectTimeOut);
            client.setSoTimeout(readWriteTimeOut);
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static int random() 
    {
        int min = 1;
        int max = (1 << 16) - 1;    // 65535,  two byte
        
        Random random = new Random();

        return random.nextInt(max) % (max - min + 1) + min;
    }
    
    public int request(Map<String, String> params, byte[] stdin) 
    {
        connect();
        
        int requestId = random();
        //System.out.println("requestId: " + requestId);
        
        builStartPacket(requestId);
        
        if ( params != null ) {
        	buildParamsPacket(requestId, params);
        }
        
        buildParamsPacket(requestId, null);
        
        if ( stdin != null ) {
        	buildStdinPacket(requestId, stdin);
        }
        
        buildStdinPacket(requestId, null);
        
        try {
            OutputStream outStream      = client.getOutputStream();
            BufferedOutputStream outBuf = new BufferedOutputStream(outStream);
            
            outBuf.write(message.sharedArray, message.offset, message.length);
            outBuf.flush();
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return requestId;
    }
    
    private void addHeader(int requestId, int type, int clen) 
    {
    	writeBytesBuffer.put( (byte) ((VERSION) & 0xFF) );             /* version */
    	writeBytesBuffer.put( (byte) ((type)    & 0xFF) );             /* type */
        
    	writeBytesBuffer.put( (byte) ((requestId >> 8) & 0xFF) );      /* requestIdB1, (& 0xFF) unsigned character!!! */
    	writeBytesBuffer.put( (byte) ((requestId)      & 0xFF) );      /* requestIdB0 */
        
    	writeBytesBuffer.put( (byte) ((clen >> 8) & 0xFF) );           /* contentLengthB1 */
    	writeBytesBuffer.put( (byte) ((clen)      & 0xFF) );           /* contentLengthB0 */
        
    	writeBytesBuffer.put( (byte) (0 & 0x00) );                     /* paddingLength */
    	writeBytesBuffer.put( (byte) (0 & 0x00) );                     /* reserved */
    }
    
    private void builStartPacket(int requestId) 
    {
        addHeader(requestId, BEGIN_REQUEST, HEADER_LEN);
        
        int loc = 0;
        
        writeBytesBuffer.put( (byte) (0 & 0x00) );         		    loc++;
        writeBytesBuffer.put( (byte) (ROLE_RESPONDER 	  & 0xFF)); loc++;
        writeBytesBuffer.put( (byte) ((keepAlive ? 1 : 0) & 0xFF)); loc++;
        
        for ( int i =  loc; i < HEADER_LEN; i++ ) {
        	writeBytesBuffer.put( (byte) (0 & 0x00) );
        }
        
        writeBytesBuffer.flip();
        message.writeToMessage(writeBytesBuffer);
        
        writeBytesBuffer.clear();
    }
    
    private void buildParamsPacket(int requestId, Map<String, String> params) 
    {
    	if ( params != null ) {
    	     for ( Map.Entry<String, String> entry : params.entrySet() ) {
    	        buildNvpair(entry.getKey(), entry.getValue());
    	     }
    	}
    	
    	readBytesBuffer.flip();
    	
    	addHeader(requestId, PARAMS, readBytesBuffer.remaining());
    	
      	writeBytesBuffer.flip();
    	
    	message.writeToMessage(writeBytesBuffer);
    	message.writeToMessage(readBytesBuffer);
    	
    	readBytesBuffer.clear();
    	writeBytesBuffer.clear();
    }
    
    private void buildStdinPacket(int requestId, byte[] stdin) 
    {
    	int clen = 0;
        if ( stdin != null ) {
        	writeBytesBuffer.put(stdin);
        	
        	clen = stdin.length;
        }
        
        addHeader(requestId, STDIN, clen);
        
        writeBytesBuffer.flip();
        message.writeToMessage(writeBytesBuffer);
        
        writeBytesBuffer.clear();
    }
    
    private void buildNvpair(String name, String value) 
    {
        int nlen = name.length();
        int vlen = value.length();
        
        if ( nlen < 128 ) {
        	readBytesBuffer.put( (byte) (nlen & 0xFF) );
        }
        else {
        	readBytesBuffer.put( (byte) ((nlen >> 24) | 0x80) );     /* nameLengthB3 */
        	readBytesBuffer.put( (byte) ((nlen >> 16) & 0xFF) );     /* nameLengthB2 */
        	readBytesBuffer.put( (byte) ((nlen >> 8)  & 0xFF) );     /* nameLengthB1 */
        	readBytesBuffer.put( (byte) ((nlen)       & 0xFF) );     /* nameLengthB0 */
        }
        
        if ( vlen < 128 ) {
        	readBytesBuffer.put( (byte) (vlen & 0xFF) );
        }
        else {
        	readBytesBuffer.put( (byte) ((vlen >> 24) | 0x80) );     /* valueLengthB3 */
        	readBytesBuffer.put( (byte) ((vlen >> 16) & 0xFF) );     /* valueLengthB2 */
        	readBytesBuffer.put( (byte) ((vlen >> 8)  & 0xFF) );     /* valueLengthB1 */
        	readBytesBuffer.put( (byte) ((vlen)       & 0xFF) );     /* valueLengthB0 */
        }
        
        readBytesBuffer.put(name.getBytes());
        readBytesBuffer.put(value.getBytes());
    }
    
    private Map<String, Integer> decodePacketHeader() 
    {
    	Map<String, Integer> ret = new HashMap<>();
    	
    	int version 	   = ((readBytes[0] & 0xFF));
    	int type    	   = ((readBytes[1] & 0xFF));

    	int responseId     = ((readBytes[2] << 8) & 0xFFFF) + ((readBytes[3]) & 0xFF); 	// byte will be forced to transfer to int when calculating(!!!)
    	int contentLength  = ((readBytes[4] << 8) & 0xFFFF) + ((readBytes[5]) & 0xFF); 	// bytes[2] & 0xFF, bytes[4] & 0xFF, (& 0xFF or & oxFFFF) must be set in last
    	int paddingLength  = ((readBytes[6] & 0xFF));
    	int reserved 	   = ((readBytes[7] & 0xFF));
    	
    	ret.put("Version", 		 version);
    	ret.put("Type", 		 type);
    	ret.put("ResponseId",    responseId);
    	ret.put("ContentLength", contentLength);
    	ret.put("PaddingLength", paddingLength);
    	ret.put("Reserved", 	 reserved);
    	
    	//System.out.println(ret);
    	return ret;
    }
    
    private int read(BufferedInputStream is, int offset, int length) 
    {
    	int bytesRead = 0;
    	
    	while ( length > 0 ) {
          	try {
				bytesRead = is.read(readBytes, offset, length);
			} 
          	catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
          	}
          	
            if ( bytesRead == -1 ) {
            	try {
					client.close();
				} 
            	catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	return -1;
            }
            length -=  bytesRead;
        }
    	
    	return length;
    }
    
    private Map<String, Integer> readPacket(BufferedInputStream is)
    {
    	Map<String, Integer> ret = null;
    	
		int bytesRead;
		
        bytesRead = read(is, 0, HEADER_LEN);
        if ( bytesRead == -1 ) return null;
        
        ret = decodePacketHeader();
        
        int clen = ret.get("ContentLength").intValue();
        bytesRead = read(is, HEADER_LEN, clen);
        if ( bytesRead == -1 ) return null;
        
        int plen  = ret.get("PaddingLength").intValue();
        bytesRead = read(is, HEADER_LEN + clen, plen);
        if ( bytesRead == -1 ) return null;
    	
    	return ret;
    }
    
    public void waitForResponse(int requestId) 
    {
    	InputStream inStream;
    	BufferedInputStream inBufStream = null;
		try {
			inStream 	= client.getInputStream();
			inBufStream = new BufferedInputStream(inStream);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        start:
    	do {
    		Map<String, Integer> ret = readPacket(inBufStream);
    		
    		if ( ret == null ) break;
    		
    		switch ( ret.get("Type").intValue() ) {
	    		case STDOUT:
	    			writeBytesBuffer.put(readBytes, HEADER_LEN, ret.get("ContentLength").intValue());
	    			readBytesBuffer.clear();
	    			break;
	    		case GET_VALUES_RESULT:	
	    			break;
	    		case STDERR:
	    		case END_REQUEST:
	    		case UNKNOWN:	
	    			break start;
    		}
    		
    	} while ( true );
        
        writeBytesBuffer.flip();
        //System.out.println(new String(writeBytesBuffer.array(), 0, writeBytesBuffer.remaining()));
        
        message.length = 0;
        message.writeToMessage(writeBytesBuffer);
        
        readBytesBuffer.clear();
        writeBytesBuffer.clear();
        
        //readBytesBuffer  = null;
        //writeBytesBuffer = null;
        try {
			client.close();
		} 
        catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
