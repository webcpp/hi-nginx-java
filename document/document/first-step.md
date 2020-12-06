# 起步

以下是hi-nginx-java内置的测试代码:
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
它是最基本的`hello world`。访问`http://localhost/hi/test.java`时，`hi.test`类将被调用。也就是说，hi-nginx-java内置的路由保证，当URI是`/a/b/c`时，被映射调用的类将是`a.b.c`。因此，hi-nginx-java内部维护着一个URI与Class之间的默认映射关系，如果没有设置其他映射关联，hi-nginx-java就会假设所需调用的类由给定的URI元素构成——如果找不到这个类，hi-nginx-java就会返回404错误。

因此，所有的web服务开发都应该围绕着`hi.route.run_t`这个接口来实现。这个接口很简单，它的定义是：
```java
    public interface run_t {
        public void handler(request req, response res, Matcher m);
    }
```
开发者需要实现`handler`方法。该方法包含三个参数，分别是http请求、http响应和一个`Matcher`。如果没有额外设置路由，这个`Matcher`其实没什么用，它并没有对URI的进一步处理。

