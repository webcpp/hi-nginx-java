PRO=hi-nginx-java.jar

JAVAC = ${JAVA_HOME}/bin/javac
JAVA_FLAGS =-classpath .:${CLASSPATH}

JAR = ${JAVA_HOME}/bin/jar
JAR_FLAGS = --create --file

all:
	cd src && \
	find . -name *.java -type f > src.list && \
	$(JAVAC) $(JAVA_FLAGS) @src.list
	cd src && \
	find . -name *.class -type f > class.list && \
	$(JAR) $(JAR_FLAGS) ${PRO} @class.list

clean:
	rm -f src/${PRO} src/src.list src/class.list

install:
	install src/$(PRO) /usr/local/nginx/java
