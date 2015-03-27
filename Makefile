compile: src/server/*.java
	rm -rf ./class
	mkdir class
	javac -d class src/server/* -d class/ -classpath lib/JSON4Java.jar:lib/mysql-connector-java.jar

run:
	java -cp class/:lib/JSON4Java.jar:lib/mysql-connector-java.jar server.WebServer2

clean:
	rm -rf ./class


