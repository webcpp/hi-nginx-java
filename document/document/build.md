# 构建应用

通常，构建工具是maven,ant或者gradle。但是，也可以只用`make`:`make clean && make -j2 && sudo make install`搞定一切。

假设开发者同时安装了java,groovy和scala三种语言开发环境，其应用可能用到三种语言中的至少一种。只要设置好了各语言相关的环境变量:`JAVA_HOME`,`GROOVY_HOME`以及`SCALA_HOME`，并根据这三项，设置好了最重要的`CLASSPATH`，那么，`Makefile`的基本写法如下：

```Makefile


PRO=app.jar

ifndef NGINX_INSTALL_DIR
NGINX_INSTALL_DIR=/usr/local/nginx
endif


JAVAC = ${JAVA_HOME}/bin/javac
JAVA_FLAGS =-classpath .:${CLASSPATH}

GROOVYC = ${GROOVY_HOME}/bin/groovyc
GROOVY_FLAGS = $(JAVA_FLAGS)


SCALAC = ${SCALA_HOME}/bin/scalac
SCALAC_FLAGS = $(JAVA_FLAGS)

JAR = ${JAVA_HOME}/bin/jar
JAR_FLAGS = cfv



default: ${PRO}

${PRO}:
	find . -name *.java -type f > java_src.list && \
	$(JAVAC) $(JAVA_FLAGS) @java_src.list
	find . -name *.groovy -type f > groovy_src.list && \
	$(GROOVYC) $(GROOVY_FLAGS) @groovy_src.list 
	find . -name *.scala -type f > scala_src.list && \
	$(SCALAC) $(SCALAC_FLAGS) @scala_src.list
	find . -name *.class -type f > class.list && \
	$(JAR) $(JAR_FLAGS) ${PRO} `cat class.list`


clean:
	rm -f ${PRO} java_src.list groovy_src.list scala_src.list `cat class.list` class.list


install:${OBJ}
	install ${PRO} $(NGINX_INSTALL_DIR)/java


```
开发者酌情修改以适配自己的需要即可。