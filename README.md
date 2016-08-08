#JAVA服务器基于JAVA NIO
I.  实现HTTP协议(服务器端已实现)

II. 实现FASTCGI协议(客户端已实现,可以调用PHP-FPM)

III. HTTP加密->HTTPS(未实现)

IV.实现WEBSOCKET(未实现)

#HTTP服务器配置
## Web document root directory
server.root = /home/will/Develop/www

## HTTP port to listen on
server.bind  = 0.0.0.0
server.port  = 8080
server.index = index.html

## Web static file extension
server.static_ext  = html,htm,xhtml,shtml,shtm,jpg,jpeg,png,gif,bmp,ico,js,css,bin,xml,tar,ppt,pdf,swf,zip,gzip,woff,svg
server.dynamic_ext = php 

## Max post size, default 4M
server.post_maxsize = 4194304

server.fastcgi_root  = /home/will/Develop/projects/app/www
server.fastcgi_bind  = 127.0.0.1
server.fastcgi_port  = 9000
server.fastcgi_index = index.php

## Encoding
server.charset = UTF-8

# 联系作者
will&lt;pan.kai@icloud.com&gt;

持续开发中... 欢迎加入
