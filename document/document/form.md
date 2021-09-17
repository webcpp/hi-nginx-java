# 表单处理
表单处理是由hi-nginx服务器自动解析完成的。

## GET和POST表单
例如：
```html

<form action="/test/form.java" method="get">
    Name:   <input type="text" name="name"><br>
    E-mail: <input type="text" name="email"><br>
            <input type="submit">
</form> 
```


```java

package test;

import hi.request;
import hi.response;
import hi.route;
import java.util.regex.Matcher;
import java.util.HashMap;
import com.google.gson.Gson;

public class form implements hi.route.run_t {
    public form() {
    }

    public void handler(hi.request req, hi.response res, Matcher m) {
        res.set_content_type("application/json");
        HashMap<String, Object> map = new HashMap<String, Object>();
        if(req.form.containsKey("name") && req.form.containsKey("email")){
            res.status = 200;
            map.put("name",req.form.get("name"));
            map.put("email",req.form.get("email"));
            map.put("status",true)
            res.content = gson.toJson(map);
        }else{
            res.status = 400;
            map.put("status",false);
            res.content = gson.toJson(map);
        }
        
    }
}

```

此例对普通的POST表单提交同样有效。



## 文件上传

文件上传一般是通过POST方法进行。此时，开发者能够获取的不是被上传文件的“本体”，而且该“本体”的临时保存路径。因此，开发者应该按需要将该“本体”移动至自定义位置。所以，文件上传对hi-nginx-java来说，只是一个文件移动操作。例如：


```html
<form action="/test/upload.java" method="post" enctype="multipart/form-data">
    <label for="file">Filename:</label>
    <input type="file" name="myfile" id="file" /> 
    <input type="submit" name="submit" value="Submit" />
</form>

```

```java

package test;

import hi.request;
import hi.response;
import hi.route;
import java.util.regex.Matcher;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import com.google.gson.Gson;

public class upload implements hi.route.run_t {
    public upload() {
    }

    public void handler(hi.request req, hi.response res, Matcher m) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        res.set_content_type("application/json");
        Gson gson = new Gson();
        if (req.form.containsKey("myfile")) {
            File old_file = new File(req.form.get("myfile"));
            File new_file = new File("html/" + old_file.getName());
            try {
                FileUtils.moveFile(old_file, new_file);
                res.status = 200;
                map.put("status", true);
                map.put("old_file", old_file.getPath());
                map.put("new_file", new_file.getPath());
                res.content = gson.toJson(map);
            } catch (IOException e) {
                res.status = 400;
                map.put("status", false);
                map.put("message", e.getMessage());
                old_file.delete();
                res.content = gson.toJson(map);
            }
        } else {
            res.status = 400;
            map.put("status", false);
            map.put("message", "upload failed.");
            res.content = gson.toJson(map);
        }

    }
}

```

上传文件的“本体”处理是由hi-nginx完成的。所以，对于较大的文件上传，需要适当的配置。可通过标准的nginx指令`client_max_body_size`和`client_body_buffer_size`配置允许上传的文件大小。

