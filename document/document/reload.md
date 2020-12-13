# 部署热更新

应用更新部署无需`reload`或者`restart`hi-nginx。hi-nginx-java能根据全局配置
```txt
route {
	lrucache {
		reflect {
			expires = 300
			size = 1024
		}
	}
}
```
自动实现热更新。关键值由"route.lrucache.reflect.expires"指定，单位是秒。

开发环境该值尽量小些，方便调试。生产环境则应该尽可能大些。

