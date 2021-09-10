# 会话

会话功能的开启，首先要配置hi-nginx。如下所示:

```nginx
    userid on;
    userid_name SESSIONID;
    userid_domain localhost;
    userid_path /;
    userid_expires 5m;
    hi_need_cookies on;
    hi_need_session on;
```

特别要注意的是，`userid_name`必须是`SESSIONID`。

`hi.request`和`hi.response`均包含一个`session`变量，前者读取旧的会话，后者负责写入新的会话。

例如:

```java

package test;

import hi.request;
import hi.response;
import hi.route;
import java.util.regex.Matcher;
import java.util.HashMap;

public class session implements hi.route.run_t {
    public session() {
    }

    public void handler(hi.request req, hi.response res, Matcher m) {
        res.set_content_type("text/plain;charset=UTF-8");
        res.status = 200;
        String key = "test";
        int value = 0;
        if (req.session.containsKey(key)) {
            value = Integer.parseInt(req.session.get(key)) + 1;
        }
        res.session.put(key, String.valueOf(value));
        res.content = String.format("hello,%d", value);
        res.status = 200;
        
    }
}

```

`test.session`类提供一个简单的客户端访问计数服务，重复访问`http://localhost/test/session.java`可看到访问次数的变化。

## 关于 cookie
hi-nginx-java的会话功能是通过cookie机制配合`SESSIONID`完成的。但是会话数据并不保存在客户端，而是在服务器端。且在服务器端，会话数据由leveldb引擎负责高速读写。如果要使用cookie保存数据，需要使用`hi.response`的`set_cookie`方法。例如:
```java

res.set_cookie("test-k", "test-v", "max-age=3; Path=/;");

```
