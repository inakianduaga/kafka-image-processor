Kafka Image Processor 
=====================

> Small image processing demo to test Kafka (w/ Avro) integration in a full frontend/backend application

A Kafka integration in a minimal but "realistic" environment, where there is a client/backend pair handling requests and interacting with Kafka, 
and additional backends doing processing over the Kafka streams. The kafka data is saved in Avro format

## Installation

All the services are dockerized and orchestrated through [docker-compose](./docker).

#### Requirements:

- docker
- docker-compose
- Free ports: The following ports need to be free on your system to run the application 
    - 3000: Frontend 
    - 8083: [Avro Schema registry](http://localhost:8083)
    - 8084: [Kafka topics / messages](http://localhost:8084)

#### Instructions

1. Build project dockerfiles: `$ docker-compose -f ./docker/docker-compose build`
2. Run project: `$ docker-compose -f ./docker/docker-compose up`
3. Point browser to [http://localhost:3000](http://localhost:3000)

## Architecture

### Client 

- CycleJS reactive UI that communicates with the backend via Websockets 
- [More details](./ui)

#### Client Backend 

- Scala Play backend that connects to the client via websockets and receives processing requests from it. 
- Forwards data to Kafka to be processed by another backend. So it behaves as a *Kafka producer*
- [More details](./ui-backend)
   
#### Image processor 

- This is a Kafka stream backend that connects to Kafka and transforms streams (processes images from image urls)
- [More details](./processor)

#### Kafka

A Kafka/Zookeeper pair that provides an event system for the application
- [More details](./docker/kafka)
