
# Simple Java Networking Framework
## Description
This is heavily inspired by javidx9's Networking in C++ series:
<br>\- Youtube: https://www.youtube.com/watch?v=2hNdkYInj4g
<br>\- GitHub: https://github.com/OneLoneCoder/olcPixelGameEngine/tree/master/Videos/Networking/Parts1%262

A simple java networking framework. This framework offers asynchronous and non-blocking networking using Java NIO asynchronous socket channels. It offers two types of interfaces: A synchronous receive Client/Server and an asynchronous receive Client/Server. The framework allow the transmission of any Serializable objects.

## Software Stack
The software stack used for this program is Java version 13.0.2. Any newer version should work and Java 8 is required as Streams are used in this program.
```
java version "13.0.2" 2020-01-14
Java(TM) SE Runtime Environment (build 13.0.2+8)
Java HotSpot(TM) 64-Bit Server VM (build 13.0.2+8, mixed mode, sharing)
```

## Quick Start
### Server
The server classes handle connections between multiple clients. To create a **Server** extend one of the following classes:
```
SampleServer extends Server
SampleServer extends AsynchronousOutServer
```
The former handles messages received synchronously and must call the update() on the main thread method to process received messages. Meanwhile, the latter handles messages received asynchronously.
The subclass must inherit the following methods:
```
boolean OnClientConnect(int client, SocketAddress address) 
//is called when a client connects to the server, 
//return value determines if connection should be accepted

void OnClientDisconnect(int client)
//is called when a client disconnects from the server

void OnMessageReceived(Serializable message, int client)
//is called when a message is processed. 
//If extending Server class, update must be called to process messages
```
**Sending Messages:**
```
void MessageClient(int id, Serializable message)
//send a Serializable object to a client specified by the given id

void MessageAllClients(Serializable message, int ignoreId)
//send a Serializable object to all clients except the specifed client ID.
//ignoreId can be set to -1 to send to all clients
```

### Client
Creating a **Client** is very similar. One of the following classes must be extended:
```
SampleClient extends Client
SampleClient extends AsynchronousOutClient
```
The difference between the two is again how messages are processed, synchrnous or asynchronous respectively.
The subclass must inherit the folowing methods:
```
void onConnect() //is called when the client succesfully connects to the Server
void onDisconnect() //is called when the client disconnects from the server
void OnMessageReceived(Serializable message)
//is called when a message is processed. 
//If extending Client class, update must be called to process messages
```

**Sending Messages:**
```
void send(Serializable message)
//send a Serializable object to the server
```
