# 安装

## jdk

从[http://jdk.java.net/](http://jdk.java.net/)下载需要的openjdk，java 8+ 版本。不要怕版本高，较高版本的jdk一般不仅能提供更好的语言特性，减少bug，还能提高运行性能。

解压缩下载的安装包。将解压出来的文件目录整个移动至`/usr/lib/jvm/`目录下。

用`ln -s`创建一个符号链接，名为`default-java`，指向上一步jdk的安装目录。

创建`/etc/ld.so.conf.d/jdk.conf`文件，内容三行：
```txt
/usr/lib/jvm/default-java/lib/server
/usr/lib/jvm/default-java/lib
/usr/lib/jvm/default-java/jre/lib/amd64/server
```

运行`sudo ldconfig`命令，保证`libjvm.so`能被hi-nginx找到。

创建`/etc/profile.d/jdk.sh`文件，内容三行:
```txt
export JAVA_HOME=/usr/lib/jvm/default-java
export CLASSPATH=/usr/local/nginx/java:/usr/local/nginx/java/hi-nginx-java.jar
export PATH=$JAVA_HOME/bin:$PATH
```

运行命令`source /etc/profile`后，一般可通过`java -version`命令验证jdk安装成功与否。 

## hi-nginx


确保已经安装完整支持c++11的gcc和g++。一般version-4.9以上为佳。

确保已经安装PCRE、zlib和OpenSSL开发库，它们是编译hi-nginx所必须的。


从[https://github.com/webcpp/hi-nginx/releases](https://github.com/webcpp/hi-nginx/releases)下载最新的安装包。

解压缩安装包。进入解压出来的文件目录，执行命令:
```shell
./configure --with-cc=gcc  --with-http_ssl_module  --with-http_v2_module  --with-http_gzip_static_module --with-http_stub_status_module  --with-stream  --with-stream_ssl_module  --with-http_realip_module --prefix=/usr/local/nginx   --enable-http-hi-cpp=YES  --enable-http-hi-java=YES --add-module=module/ngx_http_hi_module  --add-module=module/ngx_http_autoblacklist_module --add-module=3rd/ngx_http_concat_module  --add-module=3rd/ngx_http_footer_filter_module --add-module=3rd/ngx_http_trim_filter_module  --add-module=3rd/nginx-push-stream-module-0.5.4  --add-module=3rd/ngx_slab_stat

```

然后`make -j2 && sudo make install`即可。安装完成后，hi-nginx被安装至`/usr/local/nginx`目录中。执行`sudo systemctl daemon-reload && sudo systemctl enable nginx`启用hi-nginx开机启动。要运行hi-nginx,执行`sudo systemctl start nginx`即可。需牢记，hi-nginx是一种功能增强的nginx；关于后者的一切技术知识，对前者完全适用。


## 测试

编辑`/usr/local/nginx/conf/nginx.conf`，添加以下配置：

```nginx
    hi_java_classpath "-Djava.class.path=.:/usr/local/nginx/java:/usr/local/nginx/java/hi-nginx-java.jar:/usr/local/nginx/java/app.jar";
    hi_java_options "-server -d64 -Dconfig.file=java/application.conf -Dnashorn.args=--global-per-engine";
    hi_java_servlet_cache_expires 1h;
    hi_java_servlet_cache_size 1024;
    hi_java_version 11;


    location ~ \.java {
            rewrite ^/(.*)\.java$ /$1 break;
            hi_java_servlet hi/controller; 
    }

```

执行命令`sudo systemctl restart nginx`，然后访问`http://localhost/hi/test.java`。`hi.test`是hi-nginx-java内置的测试类，它对应于URI`/hi/test`。若返回`welcome to hi-nginx-java`，则说明hi-nginx-java安装成功。其中，`/usr/local/nginx/java/app.jar`指开发者自行开发的应用。

### 重要提示
- `-Djava.class.path=`部分务必添加`/usr/local/nginx/java`项，否则`-Dconfig.file=java/application.conf`项无法起作用。
- openj9 实现暂不支持。
