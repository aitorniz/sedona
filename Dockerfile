FROM ubuntu:22.04

ENV HOME /root

WORKDIR ${HOME}

RUN apt-get update && apt-get install -y \
	git \
	vim \
	curl \
	gnupg1\
	wget \
	tar \
	openjdk-8-jre-headless\
	maven
	

RUN echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list
RUN echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list
RUN curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" |  apt-key add
RUN apt-get update && apt-get install sbt

# install spark
RUN wget "https://dlcdn.apache.org/spark/spark-3.4.3/spark-3.4.3-bin-hadoop3.tgz" && tar -xf spark-3.4.3-bin-hadoop3.tgz

# install jdk
RUN wget "https://download.oracle.com/java/22/latest/jdk-22_linux-aarch64_bin.tar.gz" && tar -xf jdk-22_linux-aarch64_bin.tar.gz

# install maven
RUN wget "https://dlcdn.apache.org/maven/maven-3/3.9.8/binaries/apache-maven-3.9.8-bin.tar.gz" && tar -xf apache-maven-3.9.8-bin.tar.gz \
    && rm -f /tmp/apache-maven.tar.gz

# set up environments
ENV MAVEN_HOME=${HOME}/apache-maven-3.9.8-bin

ENV SPARK_HOME=${HOME}/spark-3.4.3-bin-hadoop3
ENV PATH=$PATH:$SPARK_HOME
ENV PYTHONPATH=$PYTHONPATH:$SPARK_HOME/python

RUN apt-get update && apt-get install -y emacs

RUN apt-get update && apt-get install -y openjdk-8-jdk


# RUN cd ${HOME}/sedona \
#   && mvn clean install -Dspark=3.4 -DskipTests 

# RUN cd ${HOME}/sedona/examples/lidar-hd \
#   && mvn clean package


