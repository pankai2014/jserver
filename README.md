#JAVA服务器基于JAVA NIO
I.  实现HTTP协议(服务器端已实现)

II. 实现FASTCGI协议(客户端已实现,可以调用PHP-FPM)

#运行http服务器
##运行开发包下jar文件
java -jar http-server-version-{version}.jar

##配置文件路径
开发包下http-server.properties

#HTTP服务器配置
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
server.fastcgi_bind  = 127.0.0.1                                &nbsp;&nbsp;&nbsp;//PHP-FPM运行IP<br />
server.fastcgi_port  = 9000                                     &nbsp;&nbsp;&nbsp;//PHP-FRM运行PORT<br />
server.fastcgi_index = index.php                                &nbsp;&nbsp;&nbsp;//服务器访问默认动态文件<br/>

## Encoding
server.charset = UTF-8

# 待开发功能
I. HTTP加密->HTTPS(未实现)

II.实现WEBSOCKET(未实现)

# 联系作者
will&lt;pan.kai@icloud.com&gt;

持续开发中... 欢迎加入
