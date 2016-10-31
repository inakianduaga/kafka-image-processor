Kafka-Playground-UI
===================

> A frontend for visualizing / producing events

### Architecture

A websocket connection is setup to the backend to send/receive messages

## Modules

### Image lookup

- [ ] Lookup speed: Controls how many images per second we query (goes from 1/5 to 5 or something like that)
- [ ] Image lookup: Will query the random image service and fetch the urls.
- [ ] Will display the URL as a small thumb
- [ ] Will send the URL through the websocket to the backend 