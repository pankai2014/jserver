package org.kaipan.www.socket.http;

import java.util.Map;
import java.util.HashMap;

public class HttpResponse
{
	public final static String SERVER_NAME = "JAVA NIO HTTP SERVER, @author will<pan.kai@icloud.com>";
	
    public final static Map<Integer, String> HTTP_HEADER_CODE = new HashMap<Integer, String>() 
    {
		private static final long serialVersionUID = 1L;
		
		{
            put(100, "100 Continue");
            put(101, "101 Switching Protocols");
            put(200, "200 OK");
            put(201, "201 Created");
            put(204, "204 No Content");
            put(206, "206 Partial Content");
            put(300, "300 Multiple Choices");
            put(301, "301 Moved Permanently");
            put(302, "302 Found");
            put(303, "303 See Other");
            put(304, "304 Not Modified");
            put(307, "307 Temporary Redirect");
            put(400, "400 Bad Request");
            put(401, "401 Unauthorized");
            put(403, "403 Forbidden");
            put(404, "404 Not Found");
            put(405, "405 Method Not Allowed");
            put(406, "406 Not Acceptable");
            put(408, "408 Request Timeout");
            put(410, "410 Gone");
            put(413, "413 Request Entity Too Large");
            put(414, "414 Request URI Too Long");
            put(415, "415 Unsupported Media Type");
            put(416, "416 Requested Range Not Satisfiable");
            put(417, "417 Expectation Failed");
            put(500, "500 Internal Server Error");
            put(501, "501 Method Not Implemented");
            put(503, "503 Service Unavailable");
            put(506, "506 Variant Also Negotiates");
        }
    };
    
    public final static Map<String, String> HTTP_MIMES_TYPE = new HashMap<String, String>() 
    {
		private static final long serialVersionUID = 2L;

		{
    		put("jpg",  "image/jpeg");
    		put("jpeg", "image/jpeg");
    		put("bmp",  "image/bmp");
    		put("ico",  "image/x-icon");
    		put("gif",  "image/gif");
    		put("png",  "image/png");
    		put("bin",  "application/octet-stream");
    		put("js",   "application/javascript");
    		put("css",  "text/css");
    		put("html", "text/html; charset=utf-8");
    		put("xml",  "text/xml");
    		put("tar",  "application/x-tar");
    		put("ppt",  "application/vnd.ms-powerpoint");
    		put("pdf",  "application/pdf");
    		put("swf",  "application/x-shockwave-flash");
    		put("zip",  "application/x-zip-compressed");
    		put("gzip", "application/gzip");
    		put("woff", "application/x-woff");
    		put("svg",  "image/svg+xml");
    	}
    };
    
    private String meta = null;
    
    public final static String protocol = "HTTP/1.1";
    private Map<String, String> headers = new HashMap<>();
    
    public void setHttpStatus(int code) 
    {
    	this.meta = protocol + " " + HTTP_HEADER_CODE.get(code) + "\r\n";
    }
    
    public void setHeader(String key, String value) 
    {
    	this.headers.put(key, value);
    }
    
    public String getHeader() 
    {
    	if ( this.meta == null ) 					this.setHttpStatus(200);
    	if ( ! this.headers.containsKey("Server") ) this.setHeader("Server", SERVER_NAME);
    	
    	if ( ! this.headers.containsKey("Content-Type")) {
    		this.setHeader("Content-Type", "text/html; charset=utf-8");
    	}
    	
    	String out = this.meta;
    	for ( Map.Entry<String, String> entry : headers.entrySet() ) {
    		out += entry.getKey() + ": " + entry.getValue() + "\r\n"; 
        }
    	
    	out += "\r\n";
    	
    	return out;
    }
}

