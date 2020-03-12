#!/bin/bash
#Absolute path to java runtime
JAVA_HOME=$(which java)
#Path to analyse directory
ANALYSE_DIRECTORY=`dirname $0`
#Let's go to that directory
cd $ANALYSE_DIRECTORY
#Read params ini file
doUpdate=$(awk '{print $1}' ./analyse.ini)
minMem=$(awk '{print $2}' ./analyse.ini)
maxMem=$(awk '{print $3}' ./analyse.ini)
withLog=$(awk '{print $4}' ./analyse.ini)
#Do update if necessary
if [ $doUpdate == "true" ]
then
	$JAVA_HOME -jar $ANALYSE_DIRECTORY/applyAnalyseUpdate.jar
fi
#Run analyse
if [ $withLog == "true" ]
then
	$JAVA_HOME -Xms64m -Xmx256m -XstartOnFirstThread -jar $ANALYSE_DIRECTORY/analyse.jar -log
else
	$JAVA_HOME -Xms64m -Xmx256m -XstartOnFirstThread -jar $ANALYSE_DIRECTORY/analyse.jar
fi



