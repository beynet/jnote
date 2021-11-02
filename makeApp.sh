#!/bin/bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu17.30.15-ca-jdk17.0.1-macosx_x64/zulu-17.jdk/Contents/Home
export JPACKAGE=/Library/Java/JavaVirtualMachines/zulu17.30.15-ca-jdk17.0.1-macosx_x64/zulu-17.jdk/Contents/Home/bin/jpackage

mvn clean package -Dmaven.test.skip=true && \
$JPACKAGE --runtime-image $JAVA_HOME -n jnote --main-class org.beynet.jnote.gui.Main --main-jar jnote-1.1.2-SNAPSHOT-jar-with-dependencies.jar --input ./target
