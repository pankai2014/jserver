# JAVA服务器基于JAVA NIO
+ 已实现HTTP协议

+ 已实现HTTPS协议 

+ 已实现FASTCGI协议(Client端)

+ 已实现WEBSOCKET协议

# 运行HTTP/HTTPS服务器
## 运行开发包下jar文件
java -jar http-server-version-{version}.jar ./http-server.properties  
java -jar https-server-version-{version}.jar ./https-server.properties

## 源码编译
http:  编译源码并运行src/org/kaipan/www/socket/http/HttpServer.java  
https: 编译源码并运行src/org/kaipan/www/socket/https/HttpsServer.java

## 配置文件路径
http:  开发包下http-server.properties  
https: 开发包下https-server.properties

# HTTP/HTTPS服务器配置
## Web document root directory
server.root = /home/will/Develop/www          &nbsp;&nbsp;&nbsp;//静态目录

## HTTP server config
server.bind  = 0.0.0.0                        &nbsp;&nbsp;&nbsp;//服务器监听IP　  
server.port  = 8080                           &nbsp;&nbsp;&nbsp;//服务器监听PORT  
server.index = index.html                     &nbsp;&nbsp;&nbsp;//服务器访问默认静态文件 

## Web static file extension
server.static_ext  = html,htm,xhtml,shtml,shtm,jpg,jpeg,png,gif,bmp,ico,js,css,bin,xml,tar,ppt,pdf,swf,zip,gzip,woff,svg

## Web dynamic file extension
server.dynamic_ext = php 

## Max post size, default 4M
server.post_maxsize = 4194304

## FastCgi config
server.fastcgi_root  = /home/will/Develop/projects/app/www      &nbsp;&nbsp;&nbsp;//动态脚本目录<br />
server.fastcgi_host  = 127.0.0.1                                &nbsp;&nbsp;&nbsp;//PHP-FPM运行IP<br />
server.fastcgi_port  = 9000                                     &nbsp;&nbsp;&nbsp;//PHP-FRM运行PORT<br />
server.fastcgi_index = index.php                                &nbsp;&nbsp;&nbsp;//服务器访问默认动态文件<br/>

## Encoding
server.charset = UTF-8

# 联系作者
will&lt;pan.kai@icloud.com&gt;
