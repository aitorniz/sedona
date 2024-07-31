#!/bin/bash

$SPARK_HOME/bin/spark-submit \
	--master local[*] \
	--jars ../../spark/common/target/sedona-spark-common-3.0_2.12-1.6.1-SNAPSHOT.jar,../../common/target/sedona-common-1.6.1-SNAPSHOT.jar,../../spark-shaded/target/sedona-spark-shaded-3.0_2.12-1.6.1-SNAPSHOT.jar \
	--packages org.datasyslab:geotools-wrapper:geotools-24.1,org.locationtech.jts:jts-core:1.19.0 target/lidar-sedona-spark-example-1.6.0.jar
