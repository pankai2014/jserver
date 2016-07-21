package org.kaipan.www.sockets.http;

import java.util.Map;
import java.util.HashMap;

public class HttpResponse
{
    public final static Map<Integer, String> HTTP_HEADER_CODE = new HashMap<Integer, String>() {
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
    
    public String protocol = "HTTP/1.1";
    public String status   = HTTP_HEADER_CODE.get(200);
    
    public String meta;
    public Map<String, String> headers = new HashMap<>();
}

