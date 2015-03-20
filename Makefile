compile: src/server/*.java
	rm -rf ./class
	mkdir class
	javac -d class src/server/* -d class/ -classpath lib/json-simple-1.1.1.jar

run:
	java -classpath class/ server.WebServer2

clean:
	rm -rf ./class


