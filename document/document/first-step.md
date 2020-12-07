# 起步

## 默认控制器

hi-nginx-java内置一个默认的控制器`hi.controller`,hi-nginx配置为:

```nginx
    location ~ \.java {
            rewrite ^/(.*)\.java$ /$1 break;
            hi_java_servlet hi/controller; 
    }

```
通过该控制器，访问`http://localhost/hi/test.java`,即可使用hi-nginx-java内置的测试服务`hi.test`:
```java
package hi;

import hi.request;
import hi.response;
import hi.route;
import java.util.regex.Matcher;

public class test implements hi.route.run_t {
    public test() {
    }

    public void handler(hi.request req, hi.response res, Matcher m) {
        res.set_content_type("text/plain;charset=utf-8");
        res.content = "welcome to hi-nginx-java\n";
        res.status = 200;
    }
}

```

hi-nginx-java内置的路由保证，当URI是`/a/b/c`时，被映射调用的类将是`a.b.c`。因此，hi-nginx-java内部维护着一个URI与Class之间的默认映射关系，如果没有设置其他映射关联，hi-nginx-java就会假设所需调用的类由给定的URI元素构成——如果找不到这个类，hi-nginx-java就会返回404错误。

因此，所有的web服务开发都应该围绕着`hi.route.run_t`这个接口来实现。这个接口很简单，它的定义是：
```java
    public interface run_t {
        public void handler(hi.request req, hi.response res, Matcher m);
    }
```
开发者需要实现`handler`方法。该方法包含三个参数，分别是http请求、http响应和一个`Matcher`。如果没有额外设置路由，这个`Matcher`其实没什么用，它并没有对URI的进一步处理。

## 自定义控制器

如果需要自定义路由配置，则应该自定义控制器。比如:

```java

package mytest;

public class controller implements hi.servlet{
    public controller(){
        hi.route.get_instance().get("^/(example1)/?",this::do_example1));
        hi.route.get_instance().post("^/(example2)/?",this::do_example2));
    }

    public void handler(hi.request req, hi.response res){
         hi.route.get_instance().run(req, res);
    }

    private void do_example1(hi.request req, hi.response res,Matcher m){
        res.set_content_type("text/plain;charset=utf-8");
        res.content = "welcome to example1.\n";
        res.status = 200;
    }

    private void do_example2(hi.request req, hi.response res,Matcher m){
        res.set_content_type("text/plain;charset=utf-8");
        res.content = "welcome to example2.\n";
        res.status = 200;
    }


}


```

然后配置hi-nginx:
```nginx
    location ~ \.java {
            rewrite ^/(.*)\.java$ /$1 break;
            hi_java_servlet mytest/controller; 
    }

```

通过GET方法访问`http://localhost/example1.java`，能使用`do_example1`服务;通过POST方法访问`http://localhost/example2.java`,则能使用`do_example2`服务。此时,`Matcher`参数是有意义的，它意味着正则匹配URI得到的`group`。

