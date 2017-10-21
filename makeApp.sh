#!/bin/bash
$JAVA_HOME/bin/javapackager -deploy -native image -srcfiles ./target/jnote-1.0.11-SNAPSHOT-jar-with-dependencies.jar -appclass org.beynet.jnote.gui.Main -name jnote -outdir ./target/app -outfile jnote
