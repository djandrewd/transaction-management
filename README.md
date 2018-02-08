# Transaction messaging. Logging system.

Your task is to create asynchronous REST service for storing application logs into data 
storage. We assume application is very heavy loaded and log message
into data store synchronously will block application for unreasonable amount of time.

## Architecture
 
1. REST service must listen on path (_/log/message_) and handle input log message.
2. Service must pass message into logging processing JMS queue.
3. Message listener must get message from queue, store it into data store, and 
   pass response to another response JMS queue. **It is crusible that message processing 
   is done in single transaction. JMS message must rollback if data store transaction fail 
   to execute.**
4. Listen response JMS queue and send storage response to the client.
   
## Data 

You are provided with next model classes:
 
 1. _ServiceMessage_ - message hold information about logging record.
 2. _ResponseMessage_ - message hold information about data store result.
 
## API (_/log/message_) 

Method : _POST_

Request parameters:
1. _payload_ - message text of log.
2. _hostname_ - hostname where application is deployed.
3. _application_ - name of application which send log entry.
4. _level_ - logging level of application
5. _timestamp_ - timestamp in UNIX time ms when log entry is created. 

Response json value of ResponseMessage.

Example:

```
POST http://localhost:8080/log/message HTTP/1.1
Accept-Encoding: gzip,deflate
Content-Type: application/x-www-form-urlencoded
Content-Length: 88
Host: localhost:8080
Connection: Keep-Alive
User-Agent: Apache-HttpClient/4.1.1 (java 1.5)

payload=Message%20from%20web&hostname=localhost&application=test-app&level=1&timestamp=1
```

response 

```
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Wed, 04 Oct 2017 15:33:04 GMT

{
  "id" : "226fb102-313e-47cd-acad-606a61c94228",
  "errorCode" : 0
}
```

## Requirements

You must not use spring-jms for transaction management. 
You may use any kind of broker, in-memory of remove. 
JMS version can be as 1.1 as long as 2.0.
You must use Spring MVC (and can use String Boot).
Your code should follow code conventions and Google code style.

Good luck!
