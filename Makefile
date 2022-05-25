PRO=hi-nginx-java.jar
PWD:=$(shell pwd)

JAVAC = ${JAVA_HOME}/bin/javac
JAVA_FLAGS =-classpath ${PWD}/3rd:${PWD}/3rd/jmustache-1.15.jar:${PWD}/3rd/config-1.4.2.jar:${CLASSPATH}

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
	install $(PRO) /usr/local/nginx/app/java
	install -t /usr/local/nginx/app/java  `find 3rd -name '*.jar'`
