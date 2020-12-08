# 模板引擎

hi-nginx-java内置了两个mustache模板引擎：[mustache.java](https://github.com/spullara/mustache.java)和[jmustache](http://github.com/samskivert/jmustache)。

以下介绍仅就[jmustache](http://github.com/samskivert/jmustache)而言。

## 字符串模板

字符串模板是最简单的情况。例如:

```java

package test;

import hi.request;
import hi.response;
import hi.route;
import java.util.regex.Matcher;
import java.util.HashMap;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

public class stringtemplate implements hi.route.run_t {
    public session() {
    }

    public void handler(hi.request req, hi.response res, Matcher m) {
        res.set_content_type("text/plain;charset=UTF-8");
        String source = "Hello {{arg}}!";
        Template tmpl = Mustache.compiler().compile(source);
        HashMap<String, Object> context = new HashMap<String, Object>();
        context.put("arg", "world");
        res.content = tmpl.execute(context);
        res.status = 200;
    }
}

```

访问`http://localhost/test/stringtemplate.java`即可获得服务。

## 文件模板

字符串模板渲染是不多见的。常见的是文件模板渲染。

例如，创建目录`/usr/local/nginx/java/templates`，用来保存模板文件。在里面创建两个模板文件`main.mustache`:
```txt

{{title}}
{{>sub.mustache}}

```
和`sub.mustache`:
```txt
{{#persons}}
{{name}}: {{age}}
{{/persons}}

```

然后编写服务`http://localhost/test/filetemplate.java`:

```java


package test;

import hi.request;
import hi.response;
import hi.route;
import java.util.regex.Matcher;
import java.util.HashMap;
import java.util.Arrays;
import java.io.File;
import java.io.FileReader;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

public class filetemplate implements hi.route.run_t {

    public filetemplate() {
    }

    public void handler(hi.request req, hi.response res, Matcher m) {
        res.set_content_type("text/plain;charset=UTF-8");
        Mustache.TemplateLoader loader = (String name) -> {
            return new FileReader(new File("java/templates", name));
        };
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("title", "文件模板渲染测试");
        Object persons = Arrays.asList(new Object() {
            String name = "张三";
            int age = 70;
        }, new Object() {
            String name = "李四";
            int age = 59;
        });
        data.put("persons", persons);
        try {
            Template tmpl = Mustache.compiler().withLoader(loader)
                    .compile(new FileReader("java/templates/main.mustache"));
            res.content = tmpl.execute(data);
            res.status = 200;
        } catch (Exception e) {
            res.content = e.getMessage();
            res.status = 500;
        }
    }

}

```

# 其他引擎
可用于java web开发的模板引擎太多了，常见且广受好评的是freemarker。如果想用freemarker,开发者可以自行添加hi-nginx配置，比如:

```nginx
    
hi_java_classpath "-Djava.class.path=.:/usr/local/nginx/java:/usr/local/nginx/java/hi-nginx-java.jar:/usr/local/nginx/java/freemarker.jar";

```
开发时,正常导入该库即可。