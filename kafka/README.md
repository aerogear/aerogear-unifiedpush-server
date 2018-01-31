# UPS and Apache Kafka 

## Getting started with Docker
[This](https://hub.docker.com/_/zookeeper/) Zookeeper image and [this](https://hub.docker.com/r/ches/kafka/) Kafka image are used for both Linux and Mac descriptions.

### Linux
Getting started with Linux is a fairly straightforward process:

```
$ docker run -d --name zookeeper --network kafka-net zookeeper:3.4
$ docker run -d --name kafka --network kafka-net -p 9092:9092 --env ZOOKEEPER_IP=zookeeper ches/kafka
```
Connect to the Kafka broker with `localhost` or the IP of the Kafka container.

### Mac
Establishing a connection between a container and a host service with Docker for Mac is slightly more convoluted. To get going, first run both the Zookeeper and Kafka containers normally:

```
$ docker run -d --name zookeeper --network kafka-net zookeeper:3.4
$ docker run -d --name kafka --network kafka-net -p 9092:9092 --env ZOOKEEPER_IP=zookeeper ches/kafka
```

Get the container's IP address:

```
$ CONTAINER_IP=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' kafka)
$ echo $CONTAINER_IP 
```
Add this IP to the `loopback 0` interface: 
```
$ sudo ifconfig lo0 alias $CONTAINER_IP
```

Connect to the broker using this address.

______ 

**Note**: If you prefer to use your host's IP address, run the following:

```
$ HOST_IP=$(ifconfig en0 | grep "inet " | cut -d " " -f2)
$ docker run -d --name zookeeper --network kafka-net zookeeper:3.4
$ docker run -d --name kafka --network kafka-net -p 9092:9092 --env ZOOKEEPER_IP=$HOST_IP --env KAFKA_ADVERTISED_HOST_NAME=$HOST_IP ches/kafka
```
Connect to the Kafka broker using your host IP.

## Getting started with Openshift
### Linux
Create a new project: 
```
oc new-project kafka-cluster
```
Create the application from the [EnMasseProject's](https://github.com/EnMasseProject/barnabas) `Kafka stateful-sets` template:
```
oc new-app -f https://raw.githubusercontent.com/EnMasseProject/barnabas/master/kafka-statefulsets/resources/openshift-template.yaml -n kafka-cluster
```
To find the cluster IP, run:

``` 
oc get svc kafka
```

______ 

**Note**: For provisioning persistent volumes, see [here](https://github.com/ppatierno/amqp-kafka-demo#deploying-the-apache-kafka-cluster). You may have to change the SELinux security context of each directory like so: 
```
chcon -R -t svirt_sandbox_file_t /path/to/dir
```

## Connecting to the server

Allow the UPS to connect to your Kafka broker by passing in the host and port (configured in the previous steps) to the Wildfly server. Use the environment variables `KAFKA_SERVICE_HOST` and `KAFKA_SERVICE_PORT`:
```
$SERVER_HOME/bin/standalone.sh -DKAFKA_SERVICE_HOST=<ip_address> -DKAFKA_SERVICE_PORT=<port>
```

For example:
```
$SERVER_HOME/bin/standalone.sh -DKAFKA_SERVICE_HOST=172.18.0.3 -DKAFKA_SERVICE_PORT=9092
```
