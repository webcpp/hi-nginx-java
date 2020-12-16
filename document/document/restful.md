# RESTful 风格

如果一定要把一般的API 服务转换成restful风格，可以自定义控制器。例如上一节的服务例子:

| 方法   | restful url | 功能           | 原 url               | 原方法 |
| ------ | ----------- | -------------- | -------------------- | ------ |
| GET    | /website    | 查询记录列表   | /website/list.java   | GET    |
| POST   | /website    | 创建一条记录   | /website/insert.java | POST   |
| GET    | /website/id | 根据id查询记录 | /website/info.java   | GET    |
| PUT    | /website/id | 根据id更新记录 | /website/update.java | POST   |
| DELETE | /website/id | 根据id删除记录 | /website/delete.java | POST   |
```java
package website;

import hi.servlet;
import hi.request;
import hi.response;
import hi.route;

import java.util.regex.Matcher;

import website.restful_init;

public class restful implements hi.servlet {

    public restful() {
        restful_init.init();
    }

    public void handler(hi.request req, hi.response res) {
        hi.route.get_instance().run(req, res);
    }

}

```
真正的业务写在`website.restful_init`里:
```java

package website;

import hi.servlet;
import hi.request;
import hi.response;
import hi.route;

import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ArrayHandler;

import com.google.gson.Gson;

import website.website;
import website.db_help;

public class restful_init {
    private static List<String> order_t = Arrays.asList("DESC", "desc", "ASC", "asc");
    private static String integer_pattern = new String("[1-9]+[0-9]*");
    private static restful_init instance = new restful_init();

    private restful_init() {
        hi.route.get_instance().get("^/website/?", this::do_get_list);
        hi.route.get_instance().get("^/website/([1-9]+[0-9]*)/?", this::do_get_one);
        hi.route.get_instance().post("^/website/?", this::do_post);
        hi.route.get_instance().put("^/website/([1-9]+[0-9]*)/?", this::do_put);
        hi.route.get_instance().delete("^/website/([1-9]+[0-9]*)/?", this::do_delete);
    }

    public static void init() {

    }

    private void do_get_list(hi.request req, hi.response res, Matcher m) {
        String sql = "SELECT * FROM `websites` ORDER BY `id` %s LIMIT ?,?;";
        Object[] params = { 0, 5 };
        if (req.form.containsKey("order")) {
            String tmp = req.form.get("order");
            if (!restful_init.order_t.contains(tmp)) {
                sql = String.format(sql, "DESC");
            } else {
                sql = String.format(sql, tmp);
            }
        } else {
            sql = String.format(sql, "DESC");
        }
        if (req.form.containsKey("start")) {
            String p1 = req.form.get("start");
            if (p1.matches(restful_init.integer_pattern)) {
                params[0] = Integer.valueOf(p1).intValue();
            }
        }
        if (req.form.containsKey("size")) {
            String p2 = req.form.get("size");
            if (p2.matches(restful_init.integer_pattern)) {
                params[1] = Integer.valueOf(p2).intValue();
            }
        }
        res.set_content_type("application/json");
        Gson gson = new Gson();
        HashMap<String, Object> map = new HashMap<String, Object>();
        try {
            QueryRunner qr = new QueryRunner(db_help.get_instance().get_data_source());
            List<website> result = qr.query(sql, new BeanListHandler<website>(website.class), params);
            map.put("status", true);
            map.put("data", result);
            res.content = gson.toJson(map);
            res.status = 200;
        } catch (SQLException e) {
            map.put("status", false);
            map.put("message", e.getMessage());
            res.content = gson.toJson(map);
            res.status = 500;
        }

    }

    private void do_get_one(hi.request req, hi.response res, Matcher m) {
        String sql = "SELECT * FROM `websites` WHERE `id`=?;";
        Object[] params = new Object[1];
        if (m.find()) {
            params[0] = Integer.valueOf(m.group(1)).intValue();
        }
        res.set_content_type("application/json");
        Gson gson = new Gson();
        HashMap<String, Object> map = new HashMap<String, Object>();
        try {
            QueryRunner qr = new QueryRunner(db_help.get_instance().get_data_source());

            List<website> result = qr.query(sql, new BeanListHandler<website>(website.class), params);
            map.put("status", true);
            map.put("data", result);
            res.content = gson.toJson(map);
            res.status = 200;
        } catch (SQLException e) {
            map.put("status", false);
            map.put("message", e.getMessage());
            res.content = gson.toJson(map);
            res.status = 500;
        }

    }

    private void do_post(hi.request req, hi.response res, Matcher m) {
        String sql = "INSERT INTO `websites`(`name`,`url`)VALUES(?,?);";
        Object[] params = { "", "" };
        if (req.form.containsKey("name") && req.form.containsKey("url")) {
            params[0] = req.form.get("name");
            params[1] = req.form.get("url");
            res.set_content_type("application/json");
            Gson gson = new Gson();
            HashMap<String, Object> map = new HashMap<String, Object>();
            try {

                QueryRunner qr = new QueryRunner(db_help.get_instance().get_data_source());
                Object[] result = qr.insert(sql, new ArrayHandler(), params);
                map.put("status", true);
                for (Object item : result) {
                    map.put("id", item);
                }
                res.content = gson.toJson(map);
                res.status = 200;
            } catch (SQLException e) {
                map.put("status", false);
                map.put("message", e.getMessage());
                res.content = gson.toJson(map);
                res.status = 500;
            }
        }
    }

    private void do_put(hi.request req, hi.response res, Matcher m) {
        String sql = "UPDATE `websites` SET `name` = ? ,`url` = ? WHERE `id` = ?;";
        Object[] params = new Object[3];
        if (req.form.containsKey("name") && req.form.containsKey("url")) {
            params[0] = req.form.get("name");
            params[1] = req.form.get("url");
            if (m.find()) {
                params[2] = Integer.valueOf(m.group(1)).intValue();
            }
            res.set_content_type("application/json");
            Gson gson = new Gson();
            HashMap<String, Object> map = new HashMap<String, Object>();
            try {
                QueryRunner qr = new QueryRunner(db_help.get_instance().get_data_source());
                int result = qr.update(sql, params);
                map.put("status", true);
                map.put("rows", result);
                res.content = gson.toJson(map);
                res.status = 200;
            } catch (SQLException e) {
                map.put("status", false);
                map.put("message", e.getMessage());
                res.content = gson.toJson(map);
                res.status = 500;
            }
        }
    }

    private void do_delete(hi.request req, hi.response res, Matcher m) {
        String sql = "DELETE FROM `websites` WHERE `id` = ?;";
        Object[] params = new Object[1];
        if (m.find()) {
            params[0] = Integer.valueOf(m.group(1)).intValue();
        }
        res.set_content_type("application/json");
        Gson gson = new Gson();
        HashMap<String, Object> map = new HashMap<String, Object>();
        try {
            QueryRunner qr = new QueryRunner(db_help.get_instance().get_data_source());
            int result = qr.update(sql, params);
            map.put("status", true);
            map.put("rows", result);
            res.content = gson.toJson(map);
            res.status = 200;
        } catch (SQLException e) {
            map.put("status", false);
            map.put("message", e.getMessage());
            res.content = gson.toJson(map);
            res.status = 500;
        }

    }
}


```

添加hi-nginx配置即可:
```nginx
    location ^~ /website {
        hi_java_servlet website/restful;
    }

```

## 题外话
对hi-nginx-java而言，restful风格只是一种写作风格，对于实际业务而言，它既不增加也不减少。