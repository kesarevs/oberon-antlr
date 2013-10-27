default:
	java -jar /usr/local/lib/antlr-4.1-complete.jar -no-listener -visitor ./oberon/Oberon.g4
	javac ./oberon/Oberon*.java
	javac *.java

clean:
	rm -rf ./oberon/*.java
	rm -rf ./oberon/*.class
	rm -rf *.class
	rm -rf ./oberon/*.tokens