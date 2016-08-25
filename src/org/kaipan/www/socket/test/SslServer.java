package org.kaipan.www.socket.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

public class SslServer
{
    SSLEngine engine               = null;
    SSLContext sslContext          = null;
    private SSLEngineResult result = null;
    
    public final static String password     = "25024466";
    public final static String keystorePath = "/home/will/workspace/ssl/keystore";
    
    private static String[] protocols    = null;
    private static String[] cipherSuites = null;
    
    private final ByteBuffer encryptedIn;
    private final ByteBuffer encryptedOut;
    private final ByteBuffer decryptedIn;
    private final ByteBuffer decryptedOut;
    
    private volatile boolean taskPending = false;
    
    private boolean singleThreaded = false;
    
    private SocketChannel socketChannel;
    
    private void initalize() 
    {
        protocols        = "SSLv3 TLSv1.2".split(" ");
        
        cipherSuites     = new String[14];
        cipherSuites[0]  = "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256";
        cipherSuites[1]  = "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA";
        cipherSuites[2]  = "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA";
        cipherSuites[3]  = "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256";
        cipherSuites[4]  = "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA";
        cipherSuites[5]  = "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA";
        cipherSuites[6]  = "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256";
        cipherSuites[7]  = "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256";
        cipherSuites[8]  = "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA";
        cipherSuites[9]  = "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA";
        cipherSuites[10] = "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA";
        cipherSuites[11] = "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA";
        cipherSuites[12] = "TLS_RSA_WITH_AES_128_CBC_SHA256";
        cipherSuites[13] = "TLS_RSA_WITH_AES_128_CBC_SHA";
    }
    
    public SslServer() 
    {
        initalize();
        
        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            
            serverChannel.bind(new InetSocketAddress("127.0.0.1", 8080));
            
            System.out.println("listen 127.0.0.1/8080...");
            
            socketChannel = serverChannel.accept();
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        setupSSL();
        
        setupEngine();
        
        int appBufSize = engine.getSession().getApplicationBufferSize();
        int netBufSize = engine.getSession().getPacketBufferSize();
        
        decryptedIn  = ByteBuffer.allocate(appBufSize);
        decryptedOut = ByteBuffer.allocate(appBufSize);
        encryptedIn  = ByteBuffer.allocate(netBufSize);
        encryptedOut = ByteBuffer.allocate(netBufSize);
    }
    
    private void setupSSL()
    {
        char keyStorePass[] = password.toCharArray();       //证书密码
        char keyPassword[]  = password.toCharArray();       //证书别名密码
        
        KeyStore kesStore     = null;
        KeyManagerFactory keyManagerFactory = null;
        
        try {
            kesStore = KeyStore.getInstance("JKS");
        } 
        catch (KeyStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        try {
            //创建JKS密钥库
            kesStore.load(new FileInputStream(keystorePath), keyStorePass);
            
            //创建管理JKS密钥库的X.509密钥管理器
            keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            
        } 
        catch (NoSuchAlgorithmException | CertificateException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        try {
            keyManagerFactory.init(kesStore, keyPassword);
            
            sslContext = SSLContext.getInstance("SSLV3");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
        } 
        catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void setupEngine() 
    {
        engine = sslContext.createSSLEngine("127.0.0.1", 8080);
        engine.setUseClientMode(false);
        engine.setNeedClientAuth(false);
        engine.setEnabledProtocols(protocols);
        
        engine.setEnabledCipherSuites(cipherSuites);
        
        try {
            engine.beginHandshake();
        } 
        catch (SSLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }
    
    private void processHandshake() throws IOException
    {
        SSLEngineResult.HandshakeStatus status; // enum
        
        if ( result == null ) {
            status = engine.getHandshakeStatus();
        }
        else {
            status = result.getHandshakeStatus();
        }
        
        int count;
        
        switch ( status ) {
            // step 2
            case NEED_TASK:
                // delegated 委托/代表
                System.out.println("Run the delegated SSL/TLS tasks");
                runDelegatedTasks();
                
                if ( ! singleThreaded ) return;
            // step 1    
            case NEED_UNWRAP:
                System.out.println("NEED_UNWRAP");
                
                // singleThreaded为true时读不到第二次数据阻塞
                count = engine.isInboundDone()
                        ? -1
                        : socketChannel.read(encryptedIn);
                System.out.println("read " + count + " bytes");
                
                // 加密的数据
                encryptedIn.flip();
               
                // 尝试把 SSL/TLS 网络数据解码到纯文本应用程序数据缓冲区序列中。
                result = engine.unwrap(encryptedIn, decryptedIn);
                
                //decryptedIn.flip();
                System.out.println(decryptedIn.position());
                //System.out.println(decryptedIn.limit());
                //System.out.println(decryptedIn.capacity());
                
                encryptedIn.compact();
                break;
            case NEED_WRAP:
                System.out.println("NEED_WRAP");
                break;
            case FINISHED:
                System.out.println("FINISHED");
            case NOT_HANDSHAKING:
                System.out.println("NOT_HANDSHAKING");
                return;
        }
        
        switch ( result.getStatus() ) {
            case BUFFER_UNDERFLOW:
                System.out.println("BUFFER_UNDERFLOW");
                return;
            case BUFFER_OVERFLOW:
                System.out.println("BUFFER_OVERFLOW");
                return;
            case CLOSED:
                System.out.println("CLOSED");
                return;
            case OK:
                System.out.println("OK");
                break;
        }
        
        processHandshake();
    }
    private void runDelegatedTasks() 
    {
        boolean singleThreaded = true;
        
        if ( singleThreaded ) {
            Runnable task;
            while ((task = engine.getDelegatedTask()) != null) {
                task.run();
            }
            
            // update the SSLEngineResult
            updateResult();
        }
        else {
            if ( ! taskPending ) {
                //taskWorker.addSocket(this);
                //setTaskPending(true);
                
                new Thread(new Runnable(){
                    public void run(){
                        System.out.println("new thread");
                        Runnable task;
                        while ((task = engine.getDelegatedTask()) != null) {
                            task.run();
                        }
                        
                        System.out.println("task end");
                    }
                }).start();
            }
        }
    }
    
    public void updateResult() {
        result = new SSLEngineResult(
                result.getStatus(),
                engine.getHandshakeStatus(),
                result.bytesProduced(),
                result.bytesConsumed());
        // SSLEngine task was completed, set its taskPending
        // to false, in case there are more tasks to be run
        // in the future. TODO: does this ever happen?
        setTaskPending(false);
    }
    
    public void setTaskPending(boolean taskPending) {
        this.taskPending = taskPending;
    }
    
    public void run() 
    {
        try {
            processHandshake();
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) 
    {
        SslServer server = new SslServer();
        
        server.run();
    }
}
