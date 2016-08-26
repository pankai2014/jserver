package org.kaipan.www.socket.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
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
import java.util.Iterator;
import java.util.Set;

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
    
    private ByteBuffer encryptedIn;
    private ByteBuffer encryptedOut;
    private ByteBuffer decryptedIn;
    private ByteBuffer decryptedOut;
    
    private volatile boolean taskPending = false;
    
    private boolean singleThreaded = true;
    
    private Selector selector = null;
    
    private SocketChannel socketChannel;
    
    private void initalize() 
    {
        protocols        = "SSLv3 TLSv1".split(" ");
        
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
        
        setupSSL();
        
        try {
            
            selector = Selector.open();
            
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            
            serverChannel.bind(new InetSocketAddress("127.0.0.1", 8080));
            
            System.out.println("listen 127.0.0.1/8080...");
            
            socketChannel = serverChannel.accept();
            
            socketChannel.configureBlocking(false);
            SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
            
            selectionKey.attach(socketChannel);
            
            if ( socketChannel.finishConnect() ) {
                setupEngine();
                
                int appBufSize = engine.getSession().getApplicationBufferSize();
                int netBufSize = engine.getSession().getPacketBufferSize();
                
                decryptedIn  = ByteBuffer.allocate(appBufSize);
                decryptedOut = ByteBuffer.allocate(appBufSize);
                encryptedIn  = ByteBuffer.allocate(netBufSize);
                encryptedOut = ByteBuffer.allocate(netBufSize);
            }
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
                //System.out.println(decryptedIn.position());
                //System.out.println(decryptedIn.limit());
                //System.out.println(decryptedIn.capacity());
                
                if ( result.getStatus() == SSLEngineResult.Status.OK ) {
                    encryptedIn.compact();
                }
                break;
            case NEED_WRAP:
                System.out.println("NEED_WRAP");
                decryptedOut.flip();
                result = engine.wrap(decryptedOut, encryptedOut);
                decryptedOut.compact();
                
                if (result.getStatus() == SSLEngineResult.Status.CLOSED) {
                    //count = flush();
                } else {
                    // flush without the try/catch,
                    // letting any exceptions propagate.
                    count = flush();
                }
                break;
            case FINISHED:
                System.out.println("FINISHED");
                return;
            case NOT_HANDSHAKING:
                System.out.println("NOT_HANDSHAKING");
                return;
        }
        
        switch ( result.getStatus() ) {
            case BUFFER_UNDERFLOW:
                System.out.println("BUFFER_UNDERFLOW");
                int netSize = engine.getSession().getPacketBufferSize();
                // Resize buffer if needed.
                if (netSize > decryptedIn.capacity()) {
                    //System.out.println("netSieze > decryptedIn.capacity()");
                    
                    ByteBuffer b = ByteBuffer.allocate(netSize);
                    //encryptedIn.flip();
                    b.put(encryptedIn);
                    encryptedIn = b;
                }
      
                // Obtain more inbound network data for src,
                // then retry the operation.
                break;
            case BUFFER_OVERFLOW:
                System.out.println("BUFFER_OVERFLOW");
                break;
            case CLOSED:
                System.out.println("CLOSED");
                return;
            case OK:
                System.out.println("OK");
                break;
        }
        
        processHandshake();
    }
    
    private int flush() 
    {
        // Selector temp = null;
        encryptedOut.flip();
        int remaining = encryptedOut.remaining();
        System.out.println("Encrypted out remaining: " + remaining);
        int countOut = 0;
        int count = 0;
        int retries = 0;
        
        while(encryptedOut.hasRemaining()){
            try {
                count = socketChannel.write(encryptedOut);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            countOut += count;
            retries++;
        }

        encryptedOut.compact();
        
        return countOut;
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
    
    public void start() {
        int times = 1;
        while ( true ) {
            try {
                int count = selector.selectNow();
                if ( count == 0 ) continue;
                
                Set<SelectionKey>     selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                
                while ( keyIterator.hasNext() ) {
                    SelectionKey key = keyIterator.next();
                    
                    if ( key.isReadable() ) {
                        // a channel is ready for reading
                        //SocketChannel channel = (SocketChannel) key.attachment();
                        
                        processHandshake();
                        System.out.println("select read: " + times + " times");
                        keyIterator.remove();
                        
                        times++;
                    } 
                }
                
                selectedKeys.clear();
                break;
            } 
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
       }
        
        ByteBuffer src = ByteBuffer.allocate(200);
        
        System.out.println("write: HTTP/1.1 200 OK");
        src.put("HTTP/1.1 200 OK\r\n\r\n".getBytes());
        
        src.flip();
        
        try {
            socketChannel.write(src);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    public void run() 
    {
        
        new Thread(new Runnable() {

            @Override
            public void run()
            {
                // TODO Auto-generated method stub
                start();
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                
                try {
                    SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
                    selectionKey.attach(socketChannel);
                } catch (ClosedChannelException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            
        }).start();;
            
    }
    
    public static void main(String[] args) 
    {
        final SslServer server = new SslServer();
        
        server.run();
    }
}
