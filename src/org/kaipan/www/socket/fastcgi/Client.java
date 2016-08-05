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
    public final static byte VERSION           = 1;
    
    public final static byte BEGIN_REQUEST     = 1;
    public final static byte ABORT_REQUEST     = 2;
    public final static byte END_REQUEST       = 3;
    public final static byte PARAMS            = 4;
    public final static byte STDIN             = 5;
    public final static byte STDOUT            = 6;
    public final static byte STDERR            = 7;
    public final static byte DATA              = 8;
    public final static byte GET_VALUES        = 9;
    public final static byte GET_VALUES_RESULT = 10;
    public final static byte UNKNOWN           = 11;
    public final static byte MAX               = UNKNOWN; 
   
    public final static byte ROLE_RESPONDER    = 1;
    public final static byte ROLE_AUTHORIZER   = 2;
    public final static byte ROLE_FILTER       = 3;
    
    public final static int HEADER_LEN         = 8;
    
    private String host = null;
    
    private int port;
    private int connectTimeOut   = 5000;
    private int readWriteTimeOut = 5000;
    
    private ByteBuffer bytes = ByteBuffer.allocate(65535);
    
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
        
        builStartPacket(requestId);
        
        buildParamsPacket(requestId, params);
        buildParamsPacket(requestId, null);
        
        buildStdinPacket(requestId, stdin);
        buildStdinPacket(requestId, null);
        
        try {
            OutputStream outStream      = client.getOutputStream();
            BufferedOutputStream outBuf = new BufferedOutputStream(outStream);
            
            outBuf.write(message.sharedArray, 0, message.length);
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        waitForResponse(requestId);
    }
    
    public void waitForResponse(int requestId) 
    {
        try {
            InputStream inStream        = client.getInputStream();
            BufferedInputStream  inBuf  = new BufferedInputStream(inStream);
            
            int ready;
            while ( (ready = inBuf.read()) != -1 ) {
                System.out.println(ready);
            }
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void addHeader(int requestId, byte type, int clen) 
    {
        bytes.put(VERSION);                                 /* version */
        bytes.put(type);                                    /* type */
        
        bytes.put( (byte) ((requestId >> 8) & 0xFF) );      /* requestIdB1 */
        bytes.put( (byte) ((requestId)      & 0xFF) );      /* requestIdB0 */
        
        bytes.put( (byte) ((clen >> 8) & 0xFF) );           /* contentLengthB1 */
        bytes.put( (byte) ((clen)      & 0xFF) );           /* contentLengthB0 */
        
        bytes.put( (byte) (0 & 0x00) );                     /* paddingLength */
        bytes.put( (byte) (0 & 0x00) );                     /* reserved */
    }
    
    public void builStartPacket(int requestId) 
    {
        addHeader(requestId, BEGIN_REQUEST, HEADER_LEN);
        
        int loc = 0;
        
        bytes.put( (byte) (0 & 0x00) );         loc++;
        bytes.put(ROLE_RESPONDER);              loc++;
        bytes.put((byte) (keepAlive ? 1 : 0));  loc++;
        
        for ( int i =  loc; i < HEADER_LEN; i++ ) {
            bytes.put( (byte) (0 & 0x00) );
        }
        
        bytes.flip();
        message.writeToMessage(bytes);
        
        bytes.clear();
    }
    
    public void buildParamsPacket(int requestId, Map<String, String> params) 
    {
        for ( Map.Entry<String, String> entry : params.entrySet() ) {
            buildNvpair(this.bytes, entry.getKey(), entry.getValue());
        }
        
        int clen = 0;
        if ( params != null ) {
            clen = bytes.position() + 1;
        }
        
        addHeader(requestId, PARAMS, clen);
        
        bytes.flip();
        message.writeToMessage(bytes.array(), clen, bytes.remaining() - clen);
        message.writeToMessage(bytes.array(), 0, clen);
        
        bytes.clear();
    }
    
    public void buildStdinPacket(int requestId, byte[] stdin) 
    {
        addHeader(requestId, STDIN, stdin.length);
        
        bytes.put(stdin);
        
        bytes.flip();
        message.writeToMessage(bytes);
        
        bytes.clear();
    }
    
    public void buildNvpair(ByteBuffer bytes, String name, String value) 
    {
        int nlen = name.length();
        int vlen = value.length();
        
        if ( nlen < 128 ) {
            bytes.put( (byte) nlen );
        }
        else {
            bytes.put( (byte) ((nlen >> 24) | 0x80) );     /* nameLengthB3 */
            bytes.put( (byte) ((nlen >> 16) & 0xFF) );     /* nameLengthB2 */
            bytes.put( (byte) ((nlen >> 8)  & 0xFF) );     /* nameLengthB1 */
            bytes.put( (byte) ((nlen)       & 0xFF) );     /* nameLengthB0 */
        }
        
        if ( vlen < 128 ) {
            bytes.put( (byte) nlen );
        }
        else {
            bytes.put( (byte) ((vlen >> 24) | 0x80) );     /* valueLengthB3 */
            bytes.put( (byte) ((vlen >> 16) & 0xFF) );     /* valueLengthB2 */
            bytes.put( (byte) ((vlen >> 8)  & 0xFF) );     /* valueLengthB1 */
            bytes.put( (byte) ((vlen)       & 0xFF) );     /* valueLengthB0 */
        }
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
        
        params.put("SCRIPT_FILENAME", "/");
        
        params.put("SCRIPT_NAME", "/index.php");
        params.put("QUERY_STRING", "");
        
        params.put("REQUEST_URI", "http://localhost:9000/index.php");
        params.put("DOCUMENT_URI", "/index.php");
        params.put("SERVER_SOFTWARE", "FastCGI/1.0");
        
        params.put("REMOTE_ADDR", "127.0.0.1");
        params.put("REMOTE_PORT", "9000");
        
        params.put("SERVER_ADDR", "127.0.0.1");
        params.put("SERVER_PORT", "80");
        
        params.put("SERVER_NAME", "");
        params.put("SERVER_PROTOCOL", "HTTP/1.1");
        
        params.put("CONTENT_TYPE", "");
        params.put("CONTENT_LENGTH", "");
        
        client.request(params, null);
    }
}
