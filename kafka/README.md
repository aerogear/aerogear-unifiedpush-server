# UPS and Apache Kafka 

## Getting started with Docker
### Linux
Getting started with Linux is a fairly straightforward process:

```
$ docker run -d --name zookeeper --network kafka-net zookeeper:3.4
$ docker run -d --name kafka --network kafka-net -p 9092:9092 --env ZOOKEEPER_IP=zookeeper ches/kafka
```
Connect to the Kafka broker by updating your `bootstrap.servers` with `localhost:9092` or the IP of the Kafka container.

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

And finally connect to the broker using this address by updating the `bootstrap.servers` in the the `consumer.props` and `producer.props`.

______ 

**Note**: If you prefer to use your host's IP address, run the following:

```
$ HOST_IP=$(ifconfig en0 | grep "inet " | cut -d " " -f2)
$ docker run -d --name zookeeper --network kafka-net zookeeper:3.4
$ docker run -d --name kafka --network kafka-net -p 9092:9092 --env ZOOKEEPER_IP=$HOST_IP --env KAFKA_ADVERTISED_HOST_NAME=$HOST_IP ches/kafka
```
Connect to the Kafka broker using your host IP by updating the `bootstrap.servers` accordingly.
