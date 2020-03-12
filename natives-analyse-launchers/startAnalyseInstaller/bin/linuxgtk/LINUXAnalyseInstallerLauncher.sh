#!/bin/sh
#this is for Linux only, litle bug on jFace buttons
export GDK_NATIVE_WINDOWS=true
#Absolute path to java runtime
JAVA_HOME=$(which java)
#Path to analyse installer directory
ANALYSE_DIRECTORY=`dirname $0`
#Let's go to that directory
cd $ANALYSE_DIRECTORY
#Run Analyse
$JAVA_HOME -jar $ANALYSE_DIRECTORY/linuxgtk-analyse-installer.jar
