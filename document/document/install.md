# 安装

请参考[https://doc.hi-nginx.com/tutorail/install.html](https://doc.hi-nginx.com/tutorail/install.html)


## 测试

编辑`/usr/local/nginx/conf/nginx.conf`，添加以下配置：

```nginx

location ~ \.jdp {
    java_load;
    java_class_path "-Djava.class.path=.:/usr/local/nginx/app/java:/usr/local/nginx/app/java/hi-nginx-java.jar:/usr/local/nginx/app/java/mariadb-java-client-2.7.4.jar:/usr/local/nginx/app/java/mysql-connector-java-8.0.26.jar:/usr/local/nginx/app/java/druid-1.2.6.jar:/usr/local/nginx/app/java/demo.jar";
    java_options "-server -d64  -Dconfig.file=/usr/local/nginx/app/java/application.conf";
    java_servlet "hi/controller";
    java_uri_pattern ".*\.jdp$";
    java_expires 1m;
    java_version 11;    
}

```

执行命令`sudo systemctl restart nginx`，然后访问`http://localhost/hi/test.jdp`。`hi.test`是hi-nginx-java内置的测试类，它对应于URI`/hi/test`。若返回`welcome to hi-nginx-java`，则说明hi-nginx-java安装成功。其中，`/usr/local/nginx/java/demo.jar`指开发者自行开发的应用。

### 重要提示
- openj9 的兼容性存疑，暂不支持。


