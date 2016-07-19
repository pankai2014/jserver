package org.kaipan.www.sockets.http;

import java.io.UnsupportedEncodingException;

import org.kaipan.www.sockets.Message;

public class HttpParser 
{
    public final static int HTTP_HEAD_MAXLEN = 8192;
    
	private final static byte[] GET    = new byte[]{'G','E','T'};
    private final static byte[] POST   = new byte[]{'P','O','S','T'};
    private final static byte[] PUT    = new byte[]{'P','U','T'};
    private final static byte[] HEAD   = new byte[]{'H','E','A','D'};
    private final static byte[] DELETE = new byte[]{'D','E','L','E','T','E'};
    
    private static final byte[] CONTENT_LENGTH = new byte[]{'C','o','n','t','e','n','t','-','L','e','n','g','t','h'};
	    
	public static boolean prepare(Message message, HttpHeader metaData) 
	{
		int endOfHeader = findNextLineBreak(message.sharedArray, message.offset, message.length);
		
		if ( endOfHeader == -1 ) return false;
		resolveHttpMethod(message.sharedArray, message.offset, metaData);
		
		boolean headerComplete = false;
		
		while ( endOfHeader != -1 ) {
		    // whether endOfHeader has reached the position of "\r\n\r\n"
		    metaData.endOfHeader = endOfHeader + 2;
		    
			if (  metaData.endOfHeader < message.length ) { 
				if ( message.sharedArray[endOfHeader + 1] == '\r' 
						&& message.sharedArray[ metaData.endOfHeader] == '\n' ) {
				    headerComplete = true;
					break;
				}
			}
			
			int prevEndOfHeader = endOfHeader + 1;
			
			metaData.headerBreakPos.add(new Integer(endOfHeader));
			endOfHeader = findNextLineBreak(message.sharedArray, prevEndOfHeader,  message.length);
			
	        if ( metaData.httpMethod == HttpHeader.HTTP_METHOD_POST
	                && matches(message.sharedArray, prevEndOfHeader, CONTENT_LENGTH) ) {
	            try {
                    findContentLength(message.sharedArray, prevEndOfHeader, endOfHeader, metaData);
                } 
	            catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
	        }
		}
		
		if ( metaData.headerBreakPos.size() > 0
		        && headerComplete == true ) {
		    if (  metaData.endOfHeader == 0 ) return false;
		    
		    metaData.bodyStartIndex = metaData.endOfHeader + 1;
		    metaData.bodyEndIndex   = metaData.bodyStartIndex + metaData.contentLength;
		    return true;
		}
		
		return false;
	}
	
	private static void findContentLength(byte[] src, int startIndex, int endIndex, HttpHeader httpHeaders) throws UnsupportedEncodingException 
	{
        int indexOfColon = findNext(src, startIndex, endIndex, (byte) ':');

        // skip spaces after colon
        int index = indexOfColon +1;
        
        while ( src[index] == ' ' ) {
            index++;
        }

        int valueStartIndex = index;
        int valueEndIndex   = index;
        boolean endOfValueFound = false;

        while ( index < endIndex && !endOfValueFound ) {
            switch( src[index] ) {
                case '0' : ;
                case '1' : ;
                case '2' : ;
                case '3' : ;
                case '4' : ;
                case '5' : ;
                case '6' : ;
                case '7' : ;
                case '8' : ;
                case '9' : { index++;  break; }

                default: {
                    endOfValueFound = true;
                    valueEndIndex = index;
                }
            }
        }

        httpHeaders.contentLength = Integer.parseInt(new String(src, valueStartIndex, valueEndIndex - valueStartIndex, "UTF-8"));
    }
	
    public static int findNext(byte[] src, int startIndex, int endIndex, byte value)
    {
        for ( int index = startIndex; index < endIndex; index++ ) {
            if( src[index] == value ) return index;
        }
        
        return -1;
    }
	
	public static int findNextLineBreak(byte[] src, int startIndex, int endIndex) 
	{
		for ( int index = startIndex; index < endIndex; index++ ) {
            if ( src[index] == '\n' ) {
                if ( src[index - 1] == '\r') {
                    return index;
                }
            }
        }
		
        return -1;
	}
	
	public static void resolveHttpMethod(byte[] src, int startIndex, HttpHeader metaData)
	{
        if ( matches(src, startIndex, GET) ) {
        	metaData.httpMethod = HttpHeader.HTTP_METHOD_GET;
            return;
        }
        
        if ( matches(src, startIndex, POST) ) {
        	metaData.httpMethod = HttpHeader.HTTP_METHOD_POST;
            return;
        }
        
        if ( matches(src, startIndex, PUT) ) {
        	metaData.httpMethod = HttpHeader.HTTP_METHOD_PUT;
            return;
        }
        
        if ( matches(src, startIndex, HEAD) ) {
        	metaData.httpMethod = HttpHeader.HTTP_METHOD_HEAD;
            return;
        }
        
        if ( matches(src, startIndex, DELETE) ) {
        	metaData.httpMethod = HttpHeader.HTTP_METHOD_DELETE;
            return;
        }
    }

    public static boolean matches(byte[] src, int offset, byte[] value) 
    {
        for ( int i = offset, n = 0; n < value.length; i++ ) {
        	if ( src[i] == ' ' ) continue;
            if ( src[i] != value[n] ) return false;
            
            n++;
        }
        
        return true;
    }
}
