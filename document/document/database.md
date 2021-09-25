# 数据库

web 开发离不开数据库。最常见的的数据库是mariadb或者mysql。

为了使用数据库，需要下载访问数据库的库。对mariadb而言，即[MariaDB Connector/J](https://repo1.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/2.7.1/mariadb-java-client-2.7.1.jar)。

下载该库，将其安装至`/usr/local/nginx/java`中。修改`/etc/profile.d/jdk.sh`中的`CLASSPATH`,添加该库:
```shell

export CLASSPATH=/usr/local/nginx/java:/usr/local/nginx/java/hi-nginx-java.jar:/usr/local/nginx/java/mariadb-java-client-2.7.1.jar


```

运行`source /etc/profile`更新环境`CLASSPATH`。

修改`/usr/local/nginx/conf/nginx.conf`:
```nginx
hi_java_classpath "-Djava.class.path=.:/usr/local/nginx/java:/usr/local/nginx/java/hi-nginx-java.jar:/usr/local/nginx/java/mariadb-java-client-2.7.1.jar:/usr/local/nginx/java/app.jar"

```

假设有一个`testdb`，其中有一张表:
```sql
CREATE TABLE `websites` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` char(20) NOT NULL DEFAULT '' COMMENT '站点名称',
  `url` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8;

```
且向该表插入些数据:
```sql
INSERT INTO `websites`(`name`,`url`) VALUES ('Google', 'https://www.google.com/'), ('淘宝', 'https://www.taobao.com/');
```
那么，给定`application.conf`中的数据库配置如下:
```txt
mariadb {
    driver = "org.mariadb.jdbc.Driver"
    url = "jdbc:mariadb://localhost:3306/testdb"
    username = root
    password = 123456
}

```

为了获得查询该表的一个服务`http://localhost/test/db.java`，可以编写类`test.db`:
```java
package test;

import hi.request;
import hi.response;
import hi.route;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.HashMap;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import org.mariadb.jdbc.MariaDbPoolDataSource;

public class db implements hi.route.run_t {

    private static class db_help {
        private static MariaDbPoolDataSource ds = null;

        private static db_help instance = new db_help();

        private db_help() {

        }

        public static db_help get_instance() {
            return db_help.instance;
        }

        public MariaDbPoolDataSource get_data_source() throws SQLException {
            if (db_help.ds != null) {
                return db_help.ds;
            }
            db_help.ds = new MariaDbPoolDataSource(hi.route.get_instance().get_config().getString("mariadb.url"));
            db_help.ds.setUser(hi.route.get_instance().get_config().getString("mariadb.username"));
            db_help.ds.setPassword(hi.route.get_instance().get_config().getString("mariadb.password"));
            return db_help.ds;
        }
    }

    public db() {

    }

    public void handler(hi.request req, hi.response res, Matcher m) {
        String sql = "SELECT * FROM `websites` ORDER BY `id` LIMIT 0,5;";
        ResultSetHandler<ArrayList<HashMap<String, Object>>> h = (ResultSet rs) -> {
            ArrayList<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
            while (rs.next()) {
                HashMap<String, Object> row = new HashMap<String, Object>();
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();

                for (int i = 0; i < cols; i++) {
                    row.put(meta.getColumnName(i + 1), rs.getObject(i + 1));
                }
                result.add(row);
            }
            return result;
        };
        try {
            QueryRunner qr = new QueryRunner(db_help.get_instance().get_data_source());
            ArrayList<HashMap<String, Object>> result = qr.query(sql, h);
            StringBuffer content = new StringBuffer();
            for (HashMap<String, Object> item : result) {
                for (HashMap.Entry<String, Object> iter : item.entrySet()) {
                    content.append(String.format("%s = %s\n", iter.getKey(), iter.getValue().toString()));
                }
                content.append("\n\n");
            }

            res.content = content.toString();
            res.status = 200;
        } catch (SQLException e) {
            res.content = e.getMessage();
            res.status = 500;
        }
    }
}
```

访问`http://localhost/test/db.java`可以获得相应数据。

利用`org.apache.commons.dbutils.handlers.MapListHandler`类，以上`handler`可进一步简化：

```java

import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.handlers.MapListHandler;

```

```java
    public void handler(hi.request req, hi.response res, Matcher m) {
        String sql = "SELECT * FROM `websites` ORDER BY `id` LIMIT 0,5;";
        try {
            QueryRunner qr = new QueryRunner(db_help.get_instance().get_data_source());
            List<Map<String, Object>> result = qr.query(sql, new MapListHandler());
            StringBuffer content = new StringBuffer();
            for (Map<String, Object> item : result) {
                for (Map.Entry<String, Object> iter : item.entrySet()) {
                    content.append(String.format("%s = %s\n", iter.getKey(), iter.getValue().toString()));
                }
                content.append("\n\n");
            }

            res.content = content.toString();
            res.status = 200;
        } catch (SQLException e) {
            res.content = e.getMessage();
            res.status = 500;
        }
    }

```

## MySQL
对mysql而言,即，[MySQL Connector/J](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.22/mysql-connector-java-8.0.22.jar)。

修改`CLASSPATH`:
```shell
export CLASSPATH=/usr/local/nginx/java:/usr/local/nginx/java/hi-nginx-java.jar:/usr/local/nginx/java/mysql-connector-java-8.0.22.jar

```
修改hi-nginx配置:
```nginx
hi_java_classpath "-Djava.class.path=.:/usr/local/nginx/java:/usr/local/nginx/java/hi-nginx-java.jar:/usr/local/nginx/java/mysql-connector-java-8.0.22.jar:/usr/local/nginx/java/app.jar"
```

添加mysql配置至`application.conf`中:
```txt
mysql {
    driver = "org.mysql.cj.jdbc.Driver"
    url = "jdbc:mysql://localhost:3306/testdb"
    username = root
    password = 123456
}

```

修改`db_help`类:
```java
import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
```
```java
    private static class db_help {
        private static MysqlConnectionPoolDataSource ds = null;

        private static db_help instance = new db_help();

        private db_help() {

        }

        public static db_help get_instance() {
            return db_help.instance;
        }

        public MysqlConnectionPoolDataSource get_data_source() throws SQLException {
            if (db_help.ds != null) {
                return db_help.ds;
            }
            db_help.ds = new MysqlConnectionPoolDataSource();
            db_help.ds.setURL(hi.route.get_instance().get_config().getString("mysql.url"));
            db_help.ds.setUser(hi.route.get_instance().get_config().getString("mysql.username"));
            db_help.ds.setPassword(hi.route.get_instance().get_config().getString("mysql.password"));
            return db_help.ds;
        }
    }
```

## Druid 连接池
添加[druid](https://repo1.maven.org/maven2/com/alibaba/druid/1.2.3/druid-1.2.3.jar)库及其hi-nginx配置:

```nginx
hi_java_classpath "-Djava.class.path=.:/usr/local/nginx/java:/usr/local/nginx/java/hi-nginx-java.jar:/usr/local/nginx/java/mysql-connector-java-8.0.22.jar:/usr/local/nginx/java/druid-1.2.3.jar:/usr/local/nginx/java/app.jar"

```
之后，修改`db_help`如下:
```java
import com.alibaba.druid.pool.DruidDataSource;
```
```java

    private static class db_help {
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
            db_help.ds.setUrl(hi.route.get_instance().get_config().getString("mysql.url"));
            db_help.ds.setUsername(hi.route.get_instance().get_config().getString("mysql.username"));
            db_help.ds.setPassword(hi.route.get_instance().get_config().getString("mysql.password"));
            return db_help.ds;
        }
    }

```
## Bean
要使用bean，要导入`BeanHandler`,`BeanListHandler`或者`BeanMapHandler`。
```java 
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.BeanMapHandler;

```
定义class`website`:
```java
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

然后重写`handler`：
```java
    public void handler(hi.request req, hi.response res, Matcher m) {
        String sql = "SELECT * FROM `websites` ORDER BY `id` LIMIT 0,5;";
        try {
            QueryRunner qr = new QueryRunner(db_help.get_instance().get_data_source());
            List<website> result = qr.query(sql, new BeanListHandler<website>(website.class));
            StringBuffer content = new StringBuffer();

            for (website item : result) {
                content.append(
                        String.format("id = %s\tname = %s\turl = %s\n", item.getId(), item.getName(), item.getUrl()));
            }

            res.content = content.toString();
            res.status = 200;
        } catch (SQLException e) {
            res.content = e.getMessage();
            res.status = 500;
        }
    }

```
若使用`BeanMapHandler`,则修改如下:
```java

    public void handler(hi.request req, hi.response res, Matcher m) {
        String sql = "SELECT * FROM `websites` ORDER BY `id` LIMIT 0,5;";
        try {
            QueryRunner qr = new QueryRunner(db_help.get_instance().get_data_source());
            Map<String, website> result = qr.query(sql, new BeanMapHandler<String, website>(website.class));
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
```