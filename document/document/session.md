# 会话

会话功能的开启，首先要配置hi-nginx。如下所示:

```nginx
    userid on;
    userid_name SESSIONID;
    userid_domain localhost;
    userid_path /;
    userid_expires 5m;
    
```

`hi.request`包括`cookies`变量，可读取旧会话。`hi.response`包含一个`set_cookie`方法，负责写入新的会话。

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
        if (req.cookies.containsKey(key)) {
            value = Integer.parseInt(req.cookies.get(key)) + 1;
        }
        res.set_cookie(key,String.valueOf(value),"max-age=3; Path=/;")
        res.content = String.format("hello,%d", value);
        res.status = 200;
        
    }
}

```

`test.session`类提供一个简单的客户端访问计数服务，重复访问`http://localhost/test/session.jdp`可看到访问次数的变化。

