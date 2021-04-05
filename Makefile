PRO=hi-nginx-java.jar

JAVAC = ${JAVA_HOME}/bin/javac
JAVA_FLAGS =-classpath .:${CLASSPATH}

JAR = ${JAVA_HOME}/bin/jar


JAR_FLAGS = cf

all:
	cd src && \
	find . -name *.java -type f > src.list && \
	$(JAVAC) $(JAVA_FLAGS) @src.list
	cd src && \
	find . -name *.class -type f > class.list && \
	$(JAR) $(JAR_FLAGS) ${PRO} `cat class.list`
	mv src/$(PRO) ./
	rm -f src/class.list src/src.list

clean:
	rm -f ${PRO}

doc:
	cd document && gitbook build

install:
	install $(PRO) /usr/local/nginx/java
	install druid-1.2.5.jar /usr/local/nginx/java
	install mariadb-java-client-2.7.2.jar /usr/local/nginx/java
	install mysql-connector-java-8.0.23.jar /usr/local/nginx/java
