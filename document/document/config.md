# 配置

hi-nginx-java的全局配置系统是通过[config](https://github.com/lightbend/config)组件构造的。关于配置文件的语法和用法，请自行参考该网址的介绍。

运行时配置可通过`hi_java_options`进行全局配置：
```nginx
hi_java_options "-server -d64 -Dconfig.file=java/application.conf";
```
其中的`-Dconfig.file`被用来指定全局配置文件`application.conf`。
该文件应该至少包含以下元素:
```txt
route {
	lrucache {
		reflect {
			expires = 300
			size = 1024
		}
	}
    error {
        40x = "/404.html"
        50x = "/50x.html"
    } 
}

template {
        directory = "java/templates"
}


```
以上内容是说：
 - 路由器的LRU缓存器的过期时间是300秒，最多缓存1024个反射对象。
 - 对可能发生的错误，系统会301重定向至`/404.html`或者`/50x.html`。
 - 所有模板文件存放的目录是`java/templates`，此一路径相对于hi-nginx的安装目录`/usr/local/nginx`而言。

当需要使用配置变量时，可通过`hi.route`的唯一实例的`get_config`方法获得配置变量表,例如:
```java

String tmpl_dir = hi.route.get_instance().get_config().getString("template.directory");

```
如果需要配置数据库，则可以在`application.conf`中添加以下内容：
```txt

mariadb {
    driver = "org.mariadb.jdbc.Driver"
    url = "jdbc:mariadb://localhost:3306/testdb"
    username = root
    password = 123456
}
```
开发者可自行修改该配置值以适配自己的数据库。若需配置其他项目，也可自行添加调用。

包含`.`，`:`,`=`或`$`等的特殊字符的变量值，应该用双引号包裹起来。

## 说明
hi-nginx-java的全局配置系统是通过[config](https://github.com/lightbend/config)组件构造的。关于配置文件的详细的语法和用法，请自行参考该网址的介绍。
