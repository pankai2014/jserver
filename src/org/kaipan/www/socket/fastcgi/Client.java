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
    
    private byte[]	   readBytes		= new byte[65535];	
    private ByteBuffer readBytesBuffer  = ByteBuffer.wrap(readBytes);
    private ByteBuffer writeBytesBuffer = ByteBuffer.allocate(65535);
    
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
        int max = (1 << 16) - 1;    // 65535
        
        Random random = new Random();

        return random.nextInt(max) % (max - min + 1) + min;
    }
    
    public void request(Map<String, String> params, byte[] stdin) 
    {
        connect();
        
        int requestId = random();
        System.out.println("requestId: " + requestId);
        
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
        
        waitForResponse(requestId);
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
    
    private int readPacket()
    {
    	int totalBytesRead = 0;
    	
    	try {
    		int bytesRead = 0;
    		
            InputStream inStream       = client.getInputStream();
            BufferedInputStream  inBuf = new BufferedInputStream(inStream);
            
            bytesRead = inBuf.read(readBytes);
            while ( bytesRead > 0 ) {
            	totalBytesRead += bytesRead;
            	
            	bytesRead = inBuf.read(readBytes);
            }
            
            if ( bytesRead == -1 ) {
            	client.close();
            }
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    	
    	return totalBytesRead;
    }
    
    public Message waitForResponse(int requestId) 
    {
        int totalBytesRead = this.readPacket();
        
        for ( int i = 0; i < totalBytesRead; ) {
        	int responseId     = ((readBytes[i + 2] << 8) & 0xFFFF) + ((readBytes[i + 3]) & 0xFF); 	// byte will be forced to transfer to int when calculating(!!!)
        	
        	if ( responseId != requestId ) continue;
        	
        	//int version 	   = ((readBytes[i] & 0xFF));
        	int type    	   = ((readBytes[i + 1] & 0xFF));

        	int contentLength  = ((readBytes[i + 4] << 8) & 0xFFFF) + ((readBytes[i + 5]) & 0xFF); 	// bytes[2] & 0xFF, bytes[4] & 0xFF, (& 0xFF or & oxFFFF) must be set in last
        	int paddingLength  = ((readBytes[i + 6] & 0xFF));
        	//int reserved 	   = ((readBytes[i + 7] & 0xFF));
        	
        	switch ( type ) {
        		case STDOUT:
        			writeBytesBuffer.put(readBytes, i + HEADER_LEN, contentLength);
        			break;
        		case GET_VALUES_RESULT:	
        			break;
        		case STDERR:
        			break;
        		case END_REQUEST:
        		case UNKNOWN:	
        			break;
        	}
        	
        	i += HEADER_LEN + contentLength + paddingLength;
        }
        
        //System.out.println(writeBytesBuffer);
        writeBytesBuffer.flip();
        System.out.println(new String(writeBytesBuffer.array(), 0, writeBytesBuffer.remaining()));
        
        message.length = 0;
        message.writeToMessage(writeBytesBuffer);
        
        readBytesBuffer.clear();
        writeBytesBuffer.clear();
        
        readBytesBuffer  = null;
        writeBytesBuffer = null;
        
        return message;
    }
    
    public static void main(String[] args) 
    {
        Client client = new Client("127.0.0.1", 9000);
        MessageBuffer messageBuffer = new MessageBuffer();
        Message message = messageBuffer.getMessage();
        client.initialize(message);
        
        Map<String, String> params = new HashMap<>();
        
        params.put("GATEWAY_INTERFACE", "FastCGI/1.0");
        params.put("REQUEST_METHOD", "GET");
        
        params.put("SCRIPT_FILENAME", "/home/will/Develop/projects/app/www/index.php");
        
        params.put("SCRIPT_NAME", "/index.php");
        params.put("QUERY_STRING", "");
        
        params.put("REQUEST_URI", "/index.php");
        params.put("DOCUMENT_URI", "/index.php");
        params.put("SERVER_SOFTWARE", "php/fcgiclient");
        
        params.put("REMOTE_ADDR", "127.0.0.1");
        params.put("REMOTE_PORT", "9000");
        
        params.put("SERVER_ADDR", "127.0.0.1");
        params.put("SERVER_PORT", "80");
        
        params.put("SERVER_NAME", "will-All-Series");
        params.put("SERVER_PROTOCOL", "HTTP/1.1");
        
        params.put("CONTENT_TYPE", "");
        params.put("CONTENT_LENGTH", "0");
        
        client.request(params, null);
    }
}
