# 与JVM脚本语言混合编程

所有能编译出java .class文件的JVM脚本语言，都能在hi-nginx-java体系中工作。

例如groovy

：
```groovy

package groovy

import hi.request
import hi.response
import hi.route
import java.util.regex.Matcher

class test  implements  hi.route.run_t {

    public void handler(hi.request req, hi.response res, Matcher m) {
        res.set_content_type('text/plain;charset=utf-8')
        res.content = 'hello,groovy\n'
        res.status = 200
    }

}

```
再例如scala:
```scala
package scala;

import hi.request
import hi.response
import hi.route
import java.util.regex.Matcher

class test extends hi.route.run_t {
  def handler(req: hi.request, res: hi.response, m: Matcher) {
    res.content = "hello,scala\n"
    res.status = 200
    res.set_content_type("text/plain;charset=utf-8")
  }
}


```

再例如kotlin:
```kotlin
package mykotlin

import hi.request
import hi.response
import hi.route
import java.util.regex.Matcher


class test:hi.route.run_t {
    override fun handler(req: hi.request, res: hi.response, m: Matcher){
        res.content = "hello,kotlin\n"
        res.status = 200
        res.set_content_type("text/plain;charset=utf-8")
    }
}
```

jvm脚本语言能加快开发进度，对于一些性能要求不甚严格的功能，完全可以胜任。无论哪种脚本，最终都需要编译成java能识别.class文件然后供给hi-nginx-java使用，性能损失其实很小——多数情况下不必在意。故而，完全可以把jvm脚本语言当做jdk的扩展库来使用。关键是配置好hi-nginx的`hi_java_classpath`指令。