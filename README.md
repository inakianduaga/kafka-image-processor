Kafka Image Processor [WIP]
===========================

> [WIP] Small image processing demo to test Kafka integration in a full frontend/backend application

The idea is to an integration of Kafka in a minimal but realistic environment, where there is a client/backend pair handling requests and interacting with Kafka, 
and additional backends doing processing over the Kafka streams

## Installation:

- Use dockerized installation through [docker-compose](./docker)

## Architecture

### Client

- CycleJS reactive UI that communicates with the backend via Websockets 
- [More details](./ui)

#### Client Backend 

- Scala Play backend that connects to the client via websockets and receives processing requests from it. 
- Forwards data to Kafka to be processed by another backend. So it behaves as a *Kafka producer*
- [More details](./ui-backend)
   
#### Image processor backend

- This is a Kafka stream backend that connects to Kafka and transforms streams (processes images from image urls)
- [More details](./processor)

#### Kafka

A Kafka/Zookeeper pair that provides an event system for the application
- [More details](./docker/kafka)


