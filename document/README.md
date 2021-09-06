# 简介
hi-nginx-java = nginx + java


所有跟tomcat之类传统的servlet容器服务器相关的东西，都失效了。

hi-nginx-java 在nginx中重新定义了servlet规范。该定义仅仅依赖于nginx处理http协议的流程。所以是先有http服务器，后有servlet定义。这一点跟传统的servlet概念正好相反。


得益于nginx优良的http并发设计，hi-nginx-java天生能提供远高于传统容器服务器的并发能力和安全能力。除了业务逻辑对内存资源和cpu资源的消耗之外，不存在为单纯高并发而出现的内存资源需求和cpu资源需求；所以，hi-nginx-java在高并发下需要的内存资源和cpu资源比传统容器服务器要少得多。

传统容器服务器一般把nginx作为反向代理服务器，基于http协议与其交互。hi-nginx-java则不然，它直接基于内存与之交互：nginx导入请求，java处理完该请求后，nginx返回响应。故而，hi-nginx-java处理请求的效率，远高于传统容器服务器。

hi-nginx-java包含的servlet定义非常“轻”，其核心API只使用java原生的`String`,`HashMap<String, String>`,`HashMap<String, ArrayList<String>>`和`regex`。这意味着，它的学习门槛非常低。由于完全可以抛开javaEE进行web开发，对开发者来说，需要学习的知识门类减少了很一大部分。

考虑到web业务开发本身的需要，除了基本的servlet协议定义之外，hi-nginx-java自带了一些第三方组件，并且不再依赖任何外部库。全部组件列表如下：
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
  - collections4
  - dbutils
  - mail
  - io
  - lang3
  - math3
  - net
  - rng
  - text
  - beanutils
  - imaging
- jakarta
  - activation
  - mail
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
