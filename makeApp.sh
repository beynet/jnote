#!/bin/bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-14.0.1.jdk/Contents/Home

mvn clean package -Dmaven.test.skip=true && \
$JAVA_HOME/bin/jpackage --runtime-image $JAVA_HOME  -n jnote --main-class org.beynet.jnote.gui.Main --main-jar jnote-1.1.1-jar-with-dependencies.jar --input ./target