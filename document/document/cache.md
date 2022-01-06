# 缓存加速

通常，开发者会使用redis等内存数据库作为缓存加速的首选手段。但是对hi-nginx-java来说，这些反而是次优的选择。hi-nginx本身即包含缓存加速器，无需额外与其他服务器建立连接即可实现高速缓存加速。


请参考[https://doc.hi-nginx.com/tutorail/lru_cache.html](https://doc.hi-nginx.com/tutorail/lru_cache.html)