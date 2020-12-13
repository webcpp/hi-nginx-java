# 缓存加速

通常，开发者会使用redis等内存数据库作为缓存加速的首选手段。但是对hi-nginx-java来说，这些反而是次优的选择。hi-nginx本身即包含缓存加速器，无需额外与其他服务器建立连接即可实现高速缓存加速。



## 系统缓存加速

系统缓存加速机制包含在hi-nginx的工作进程之中。只需添加hi-nginx的四个配置项，即可实现：

```nginx
            hi_need_cache on;
            hi_cache_expires 5s;
            hi_cache_method GET;
            hi_cache_size 50;

```
`hi_need_cache`是加速开关，打开即可。其他三项根据需要设置即可。

例如，访问`/website/info.java?id=10`，如果关闭系统加速机制，`ab`压测吞吐率RPS仅有数千而已：
```txt
Server Software:        nginx/1.19.5
Server Hostname:        localhost
Server Port:            80

Document Path:          /website/info.java?id=10
Document Length:        51 bytes

Concurrency Level:      100
Time taken for tests:   2.922 seconds
Complete requests:      10000
Failed requests:        0
Keep-Alive requests:    9949
Total transferred:      3439745 bytes
HTML transferred:       510000 bytes
Requests per second:    3422.31 [#/sec] (mean)
Time per request:       29.220 [ms] (mean)
Time per request:       0.292 [ms] (mean, across all concurrent requests)
Transfer rate:          1149.60 [Kbytes/sec] received



```

若开启该机制，吞吐率RPS会暴涨至十倍以上:
```txt
Server Software:        nginx/1.19.5
Server Hostname:        localhost
Server Port:            80

Document Path:          /website/info.java?id=10
Document Length:        51 bytes

Concurrency Level:      100
Time taken for tests:   0.216 seconds
Complete requests:      10000
Failed requests:        0
Keep-Alive requests:    9970
Total transferred:      3899850 bytes
HTML transferred:       510000 bytes
Requests per second:    46196.63 [#/sec] (mean)
Time per request:       2.165 [ms] (mean)
Time per request:       0.022 [ms] (mean, across all concurrent requests)
Transfer rate:          17593.74 [Kbytes/sec] received

```

## 用户缓存加速

用户缓存加速通过hi-nginx的以下三项配置控制：
```nginx
    hi_need_kvdb on;
    hi_kvdb_size 50;
    hi_kvdb_expires 5s;
```
这时，需要修改`/website/info.java`对应的服务类:
```java
import org.apache.commons.codec.digest.DigestUtils;
```
```java
    public void handler(hi.request req, hi.response res, Matcher m) {
        if (req.method.equals("GET")) {
            String cache_k = DigestUtils.md5Hex(req.uri+req.param);
            if(req.cache.containsKey(cache_k)){
                res.content = req.cache.get(cache_k);
                res.status = 200;
            }else{
                String sql = "SELECT * FROM `websites` WHERE `id`=?;";
                Object[] params = new Object[1];
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
                        res.cache.put(cache_k, res.content);
                    } catch (SQLException e) {
                        res.content = e.getMessage();
                        res.status = 500;
                    }
                }
            }

            
        }
    }
```
此时，`ab`压测吞吐率RPS也能有数倍的提升：
```txt
Document Path:          /website/info.java?id=10
Document Length:        51 bytes

Concurrency Level:      100
Time taken for tests:   0.601 seconds
Complete requests:      10000
Failed requests:        0
Keep-Alive requests:    9916
Total transferred:      3439580 bytes
HTML transferred:       510000 bytes
Requests per second:    16625.57 [#/sec] (mean)
Time per request:       6.015 [ms] (mean)
Time per request:       0.060 [ms] (mean, across all concurrent requests)
Transfer rate:          5584.47 [Kbytes/sec] received
```
用户缓存加速可与系统缓存加速协调工作。