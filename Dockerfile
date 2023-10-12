FROM amazoncorretto:17

#RUN apt-get update
#RUN apt-get install -y gcc
#
#RUN apt-get install -y netcat
#RUN apt-get install -y iputils-ping

#RUN apt install default-jre


WORKDIR /app/

ADD src/local/Main.java /app/local/
ADD src/local/CommunicationChannels.java /app/local/
ADD src/local/OpenListener.java /app/local/
ADD src/local/OpenTalker.java /app/local/
ADD src/local/StateValue.java /app/local/

ADD jackson-core-2.15.2.jar /app/
ADD jackson-annotations-2.15.2.jar /app/
ADD jackson-databind-2.15.2.jar /app/

ADD hostsfile.txt /app/

#
#RUN javac -cp jackson-core-2.15.2.jar:jackson-annotations-2.15.2.jar:jackson-databind-2.15.2.jar:/app/local/ /app/local/TokenJSON.java
#RUN javac -cp jackson-core-2.15.2.jar:jackson-annotations-2.15.2.jar:jackson-databind-2.15.2.jar:/app/local/ /app/local/OpenTalker.java
#
#RUN javac -cp jackson-core-2.15.2.jar:jackson-annotations-2.15.2.jar:jackson-databind-2.15.2.jar /app/localOpenListener.java
#RUN javac -cp jackson-core-2.15.2.jar:jackson-annotations-2.15.2.jar:jackson-databind-2.15.2.jar /app/local/CommunicationChannels.java
#RUN javac -cp jackson-databind-2.15.2-javadoc.jar:jackson-databind-2.15.2.jar:jackson-databind-2.15.2.jar /app/local/Main.java



RUN javac /app/local/StateValue.java
RUN javac /app/local/OpenTalker.java
RUN javac /app/local/OpenListener.java
RUN javac /app/local/CommunicationChannels.java
RUN javac /app/local/Main.java

ENTRYPOINT ["java", "local/Main"]
