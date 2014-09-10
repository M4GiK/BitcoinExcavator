#!/bin/bash

if type -p java; then
    echo found java executable in PATH
    _java=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    echo found java executable in JAVA_HOME     
    _java="$JAVA_HOME/bin/java"
else
    echo "You do not have Java installed on your machine. Java 1.8 is required."
fi

if [[ "$_java" ]]; then
    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo version "$version"
    if [[ "$version" > "1.8" ]]; then
	echo version is more than 1.8
	if type -p mvn; then
	    echo found mvn executable in PATH
	    echo Installing process...
		mvn clean install
	fi
    else         
	echo "You do not have Java 1.8 installed on your machine. Java 1.8 is required."
    fi
fi
