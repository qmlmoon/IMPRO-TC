IMPRO-Text Categorisation
=========

###Group Member
Mingliang Qi

Xiaowei Jiang

Xi Yang

###Supervisor
Dr. Holmer Hemsen


###Description

TODO: blabla

###Setup Guide
In order to run this application you will need following tools:

- [Java 7 SE](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
- [Maven 3](http://maven.apache.org/download.cgi)
- [Docker](https://docs.docker.com/installation/)

To build the project, just simply type:
```
mvn package -DskipTests
```
Then you will find the jar files in `target/` directory.
The next step is to run Docker container type:

```
sudo docker run -p 127.0.0.1:5672:5672 -t -i flinkstreaming/flink-connectors-rabbitmq
```

Now a terminal started running from the image with all the necessary configurations to test run the RabbitMQ connector. The -p flag binds the localhost’s and the Docker container’s ports so RabbitMQ can communicate with the application through these.

To start the RabbitMQ server, which we used to simulate the stream data:
```
sudo /etc/init.d/rabbitmq-server start
```

Now you are ready to run our application on your host computer:
```
java -cp target/*-jar-with-dependencies impro.tc.TextCategorisation 
```
Finally feed the local batch data into the stream pipe:
```
java -cp target/*-jar-with-dependencies impro.tc.SimulateStreamNews
```