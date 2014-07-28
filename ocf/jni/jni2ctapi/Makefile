#
# Makefile for jni2ctapi project
#

VERS    = 1.2.0

all:    libjni2ctapi-i386.so libjni2ctapi-amd64.so

install:
	cp libjni2ctapi.so $(JAVA_HOME)/jre/lib/i386

clean:
	-rm *.o
	-rm *.class
	-rm *.so
	-rm *~

libjni2ctapi-amd64.so:        ctapi_wrapper.cpp ctapi_wrapper.h
	g++ -I $(JAVA_HOME)/include -I $(JAVA_HOME)/include/linux ctapi_wrapper.cpp -o libjni2ctapi-amd64.so -shared -ldl -fPIC -m64
	
libjni2ctapi-i386.so:        ctapi_wrapper.cpp ctapi_wrapper.h
	g++ -I $(JAVA_HOME)/include -I $(JAVA_HOME)/include/linux ctapi_wrapper.cpp -o libjni2ctapi-i386.so -shared -ldl -m32
