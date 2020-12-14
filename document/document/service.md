# 面向服务

对hi-nginx-java而言，服务即URI指向的实体类。因为URI与Class之间的映射关系，所以所谓面向服务即提供URI`/a/b/c`指向的Class`a.b.c`。

比如，对表`websites`提供CRUD服务.如果规划的URI包括:`/website/info`,`/website/insert`,`/website/update`,`/website/delete`,`/website/list`，那么对应的Class应该有:`website.info`，`website.insert`,`website.update`,`website.delete`和`website.list`。

例如，对于`/website/list`:

```java

package website;

public class website {
    private int id;
    private String url;
    private String name;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

```

```java
package website;

import java.sql.SQLException;
import com.alibaba.druid.pool.DruidDataSource;

public class db_help {
    private static DruidDataSource ds = null;

    private static db_help instance = new db_help();

    private db_help() {

    }

    public static db_help get_instance() {
        return db_help.instance;
    }

    public DruidDataSource get_data_source() throws SQLException {
        if (db_help.ds != null) {
            return db_help.ds;
        }
        db_help.ds = new DruidDataSource();
        db_help.ds.setUrl(hi.route.get_instance().get_config().getString("mariadb.url"));
        db_help.ds.setUsername(hi.route.get_instance().get_config().getString("mariadb.username"));
        db_help.ds.setPassword(hi.route.get_instance().get_config().getString("mariadb.password"));
        return db_help.ds;
    }
}

```
URI`/website/list`对应于`website.list`，默认列表前五条：

```java

package website;

import hi.request;
import hi.response;
import hi.route;

import java.util.regex.Matcher;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import website.website;
import website.db_help;

public class list implements hi.route.run_t {
    private static List<String> order_t = Arrays.asList("DESC", "desc", "ASC", "asc");

    public list() {
    }

    public void handler(hi.request req, hi.response res, Matcher m) {
        if (req.method.equals("GET")) {
            String sql = "SELECT * FROM `websites` ORDER BY `id` %s LIMIT ?,?;";
            Object[] params = { 0, 5 };
            if (req.form.containsKey("order")) {
                String tmp = req.form.get("order");
                if (!list.order_t.contains(tmp)) {
                    sql = String.format(sql, "DESC");
                } else {
                    sql = String.format(sql, tmp);
                }
            } else {
                sql = String.format(sql, "DESC");
            }
            if (req.form.containsKey("start")) {
                params[0] = Integer.valueOf(req.form.get("start")).intValue();
            }
            if (req.form.containsKey("size")) {
                params[1] = Integer.valueOf(req.form.get("size")).intValue();
            }
            res.set_content_type("text/plain;charset=UTF-8");
            try {
                QueryRunner qr = new QueryRunner(db_help.get_instance().get_data_source());

                List<website> result = qr.query(sql, new BeanListHandler<website>(website.class), params);
                StringBuffer content = new StringBuffer();

                for (website item : result) {
                    content.append(String.format("id = %s\tname = %s\turl = %s\n", item.getId(), item.getName(),
                            item.getUrl()));
                }

                res.content = content.toString();
                res.status = 200;
            } catch (SQLException e) {
                res.content = e.getMessage();
                res.status = 500;
            }
        }
    }
}


```
对`/website/info`：
```java
package website;

import hi.request;
import hi.response;
import hi.route;

import java.util.regex.Matcher;
import java.util.Map;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanMapHandler;

import website.website;
import website.db_help;

public class info implements hi.route.run_t {
    public info() {
    }

    public void handler(hi.request req, hi.response res, Matcher m) {
        if (req.method.equals("GET")) {
            String sql = "SELECT * FROM `websites` WHERE `id`=?;";
            Object[] params = { 1 };
            if (req.form.containsKey("id")) {
                params[0] = Integer.valueOf(req.form.get("id")).intValue();
                try {
                    QueryRunner qr = new QueryRunner(db_help.get_instance().get_data_source());

                    Map<String, website> result = qr.query(sql, new BeanMapHandler<String, website>(website.class),
                            params);
                    StringBuffer content = new StringBuffer();

                    for (Map.Entry<String, website> item : result.entrySet()) {
                        content.append(String.format("%s\tid = %s\tname = %s\turl = %s\n", item.getKey(),
                                item.getValue().getId(), item.getValue().getName(), item.getValue().getUrl()));
                    }

                    res.content = content.toString();
                    res.status = 200;
                } catch (SQLException e) {
                    res.content = e.getMessage();
                    res.status = 500;
                }
            }
        }
    }
}


```
URI`/website/insert`对应于`website.insert`：
```java

package website;

import hi.request;
import hi.response;
import hi.route;

import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayHandler;

import com.google.gson.Gson;

import website.website;
import website.db_help;

public class insert implements hi.route.run_t {
    public insert() {
    }

    public void handler(hi.request req, hi.response res, Matcher m) {
        if (req.method.equals("POST")) {
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
    }
}


```
URI`/website/update`对应于`website.update`：
```java

package website;

import hi.request;
import hi.response;
import hi.route;

import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;

import com.google.gson.Gson;

import website.website;
import website.db_help;

public class update implements hi.route.run_t {
    public update() {
    }

    public void handler(hi.request req, hi.response res, Matcher m) {
        if (req.method.equals("POST")) {
            String sql = "UPDATE `websites` SET `name` = ? ,`url` = ? WHERE `id` = ?;";
            Object[] params = new Object[3];
            if (req.form.containsKey("name") && req.form.containsKey("url") && req.form.containsKey("id")) {
                params[0] = req.form.get("name");
                params[1] = req.form.get("url");
                params[2] = Integer.valueOf(req.form.get("id")).intValue();
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
    }
}


```
URI`/website/delete`对应于`website.delete`：
```java
package website;

import hi.request;
import hi.response;
import hi.route;

import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;

import com.google.gson.Gson;

import website.website;
import website.db_help;

public class delete implements hi.route.run_t {
    public delete() {
    }

    public void handler(hi.request req, hi.response res, Matcher m) {
        if (req.method.equals("POST")) {
            String sql = "DELETE FROM `websites` WHERE `id` = ?;";
            Object[] params = new Object[1];
            if (req.form.containsKey("id")) {
                params[0] = Integer.valueOf(req.form.get("id")).intValue();
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
    }
}

```