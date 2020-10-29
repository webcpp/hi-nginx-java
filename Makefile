PRO=hi-nginx-java.jar

all:
	cd src && \
	find . -name *.java -type f > src.list && \
	javac @src.list
	cd src && \
	find . -name *.class -type f > class.list && \
	jar --create --file ${PRO} @class.list

clean:
	rm -f src/${PRO} src/src.list src/class.list

install:
	install src/$(PRO) /usr/local/nginx/java