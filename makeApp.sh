#!/bin/bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-9/Contents/Home/

mvn clean package -Dmaven.test.skip=true && \
$JAVA_HOME/bin/javapackager -deploy -native image -srcdir target -srcfiles jnote-1.1.1-jar-with-dependencies.jar -appclass org.beynet.jnote.gui.Main -name jnote -outdir ./target/app -outfile jnote && \
cd target/app/ && \
hdiutil create -size 300m -fs HFS+ -volname "jnote" jnote-w.dmg && \
DEVS=$(hdiutil attach jnote-w.dmg | cut -f 1) && \
DEV=$(echo $DEVS | cut -f 1 -d ' ') && \
{
echo "$DEV"
cp -rf jnote.app /Volumes/jnote/
hdiutil detach $DEV
hdiutil convert jnote-w.dmg -format UDZO -o jnote.dmg
echo "ok"
}