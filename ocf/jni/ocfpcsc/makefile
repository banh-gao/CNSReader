#
# Make libOCFPCSC.so
#


libOCFPCSC1.so: OCFPCSC1.cpp PcscContexts.cpp PcscExceptions.cpp Tracer.cpp fieldIO.cpp
	gcc -I/usr/local/include/PCSC -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -shared -o libOCFPCSC1.so -lstdc++ -lpcsclite OCFPCSC1.cpp PcscContexts.cpp PcscExceptions.cpp Tracer.cpp fieldIO.cpp
