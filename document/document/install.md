# 安装

## jdk

从[http://jdk.java.net/](http://jdk.java.net/)下载需要的openjdk，至少java 8 版本，推荐 java 11+。不要怕版本高，较高版本的jdk一般不仅能提供更好的语言特性，减少bug，还能提高运行性能。

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

确保已经安装PCRE、zlib和OpenSSL开发库，它们是编译hi-nginx所必须的:
`yum install pcre-devel openssl-devel`

或者

`apt-get install libpcre3-dev libssl-dev`


从[https://github.com/webcpp/hi-nginx/releases](https://github.com/webcpp/hi-nginx/releases)下载最新的安装包。

解压缩安装包。进入解压出来的文件目录，执行命令:
```shell
./configure --with-cc=gcc  --with-http_ssl_module  --with-http_v2_module  --with-http_gzip_static_module --with-http_stub_status_module  --with-stream  --with-stream_ssl_module  --with-http_realip_module --prefix=/usr/local/nginx   --enable-http-hi-cpp=YES  --enable-http-hi-java=YES --add-module=module/ngx_http_hi_module  --add-module=module/ngx_http_autoblacklist_module --add-module=3rd/ngx_http_concat_module  --add-module=3rd/ngx_http_footer_filter_module --add-module=3rd/ngx_http_trim_filter_module  --add-module=3rd/nginx-push-stream-module-0.5.4  --add-module=3rd/ngx_slab_stat

```

然后`make -j2 && sudo make install`即可。安装完成后，hi-nginx被安装至`/usr/local/nginx`目录中。执行`sudo systemctl daemon-reload && sudo systemctl enable nginx`启用hi-nginx开机启动。要运行hi-nginx,执行`sudo systemctl start nginx`即可。需牢记，hi-nginx是一种功能增强的nginx；关于后者的一切技术知识，对前者完全适用。


## 测试

编辑`/usr/local/nginx/conf/nginx.conf`，添加以下配置：

```nginx
    hi_java_classpath "-Dconfig.file=java/application.conf -Djava.class.path=.:/usr/local/nginx/java:/usr/local/nginx/java/hi-nginx-java.jar:/usr/local/nginx/java/app.jar";
    hi_java_options "-server -d64";
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
- openj9 的兼容性存疑，暂不支持。


### 启用环境变量配置第三方包
如果把所有需要引用的第三方包都写在`hi_java_classpath`命令里，难免显得冗长，且不易维护。从v2.1.2.8开始，hi-nginx支持通过`env CLASSPATH`指令从环境变量读取第三方包。这将极大地简化包依赖维护工作。具体做法如下：

- 配置`/etc/systemd/system/nginx.service`,去掉`EnvironmentFile`命令前的`#`。
- 新建`/usr/local/nginx/conf/env.conf`文件，内容为`CLASSPATH=***`具体值由系统的`$CLASSPATH`变量指定。所有依赖包均可以在此处添加。
- 配置`/usr/local/nginx/conf/nginx.conf`，添加`env CLASSPATH`指令。

如此，`hi_java_classpath`指令无需修改，所有依赖配置变化都通过`env.conf`文件修改实现。只需`reload`或者`restart`即可重新加载全新配置。

### 启用环境变量配置JVM参数
对于JVM调优需求，除了`hi_java_options`指令之外，还可以通过环境变量`JVMOPTIONS`来配置，方法是在`/usr/local/nginx/conf/env.conf`中添加新一行：`JVMOPTINS=***`，例如`JVMOPTIONS=-Xms1024m -Xmx1024m`，然后添加`env JVMOPTIONS`指令。`reload`或者`restart`即可重新加载全新配置。