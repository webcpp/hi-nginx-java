# hi-nginx-java

a java web framework for hi-nginx:

- QUICK
- EASY
- FAST

## dependence

- jdk 8+

## components

- hi
  - hi.request
  - hi.response
  - hi.route
  - hi.servlet
  - hi.lrucache
  - hi.controller
- apache commons
  - codec
  - collections
  - dbutils
  - io
  - lang
  - math
  - net
  - rng
  - text
  - beanutils
- google
  - gson
- FasterXML
  - jackson-core
  - jackson-databind
  - jackson-annotations
- [msgpack-java](https://github.com/msgpack/msgpack-java)
- mustache template engine
  - [mustache.java](https://github.com/spullara/mustache.java)
  - [jmustache](http://github.com/samskivert/jmustache)
- config
  - [config](https://github.com/lightbend/config)
- [JSON-java](https://github.com/stleary/JSON-java)
  

## install

`make && sudo make install`

## hello world

```java

package hi;

import hi.request;
import hi.response;
import hi.route;
import java.util.regex.Matcher;

public class helloworld implements hi.route.run_t {
    public helloworld() {
    }

    public void handler(hi.request req, hi.response res, Matcher m) {
        res.set_content_type("text/plain;charset=utf-8");
        res.content = "hello,world\n";
        res.status = 200;
    }
}

```

```nginx
location ~ \.java {
    rewrite ^/(.*)\.java$ /$1 break;
    hi_java_servlet hi/controller;
}

```

`curl http://localhost/hi/helloworld.java`


## example
[https://github.com/webcpp/jdemo](https://github.com/webcpp/jdemo)

## document

[https://hi-nginx-java.hi-nginx.com/](https://hi-nginx-java.hi-nginx.com/)
