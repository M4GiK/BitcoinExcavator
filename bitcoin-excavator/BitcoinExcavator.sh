#!/bin/bash

if [ "$(uname)" == "Darwin" ]; then
	# Do under Mac OS X platform
	export GPU_USE_SYNC_OBJECTS=1
	cd $(dirname ${0})
		exec java -Xmx32m -cp excavator/target/libs/*:excavator/target/excavator.jar -Djava.awt.headless=true -Djava.library.path=excavator/target/libs/natives -jar excavator/target/excavator.jar $@	      
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
	# Do under Linux platform
	export GPU_USE_SYNC_OBJECTS=1
	export DISPLAY=`echo $DISPLAY | sed 's/\.[0-9]//'`
	export COMPUTE=$DISPLAY
	cd $(dirname ${0})
	exec java -Xmx32m -cp excavator/target/libs/*:excavator/target/excavator.jar -Djava.library.path=excavator/target/libs/natives -jar excavator/target/excavator.jar $@
elif [ "$(expr substr $(uname -s) 1 5)" == "SunOS" ]; then
    	# Do under Solaris platform
	export GPU_USE_SYNC_OBJECTS=1
	export DISPLAY=`echo $DISPLAY | sed 's/\.[0-9]//'`
	export COMPUTE=$DISPLAY
	cd $(dirname ${0})
	exec java -Xmx32m -cp excavator/target/libs/*:excavator/target/excavator.jar -Djava.library.path=excavator/target/libs/natives -jar excavator/target/excavator.jar $@
elif [ "$(expr substr $(uname -s) 1 6)" == "CYGWIN" ]; then
   	# Do under Windows platform
	echo $0: this script does not support Windows yet \:\(
elif [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
	# Do under Windows NT platform
	echo $0: this script does not support Windows yet \:\(
fi

