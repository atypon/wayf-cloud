[![Build Status](https://travis-ci.org/Atypon-OpenSource/wayf-cloud.svg?branch=development)](https://travis-ci.org/Atypon-OpenSource/wayf-cloud)
[![codecov](https://codecov.io/gh/Atypon-OpenSource/wayf-cloud/branch/development/graph/badge.svg)](https://codecov.io/gh/Atypon-OpenSource/wayf-cloud)

# WAYF Cloud
## Introduction
The where-are-you-from (WAYF) service is intended to reduce, or eliminate where possible, the friction that users/researchers experience as they try to access content on different publisher platforms where they are presented with a very large number of access options (e.g login, signup, institutional access, Social Logins etc). This goal is achieved by establishing an architecture and methods where content providers can access information about the user's browsing/authentication history and make intelligent decisions about the options which will allow the users to access their content, thus reducing the access options to a minimal set that apply for the given user profile.

For more information on the project, please visit [wayf-cloud.readme.io](https://wayf-cloud.readme.io/docs).

## Solution Overview
### API
The wayf-cloud is accessible via a RESTful API. The inbound requests are serviced with [vert.x](http://vertx.io), a lightweight event-driven framework focused on throughput. Vert.x listens for inbound requests on an event loop. These events are immediately consumed and moved off of the event loop, freeing it up to accept more requests. After processing, the response is able to be written on any available thread. Due to powerful design but small footprint, vert.x is able to process thousands of requests per second.

### Reactive Back-end
Each client request is processed asynchronously on the back-end via the use of [ReactiveX](http://reactivex.io/). ReactiveX provides a functional framework for processing requests via observable streams. Asynchronous processing and concurrency are central to the library's implmentation. This abstracts away low-level threading and concurrency issues while exposing useful hooks in deciding where to execute logic. For instance, long running IO bound work, such as database reads, can be performed in an unbounded IO threadpool. Work that is more CPU intensive can be performed in a threadpool optimized for the machine's processor. The end result is an implemantation that is concise, easy to read, and maximizes performance.

## Building and Running WAYF Cloud
### Service Dependencies
1. MySQL (Version 5.6.+)
2. Redis
3. Java 8
4. Maven

### Environment Configuration
The WAYF cloud is configured via a properties file named `wayf.properties`. A typical file may look like:
```properties
jdbc.driver=com.mysql.jdbc.Driver
jdbc.username=root
jdbc.password=test
jdbc.url=jdbc:mysql://localhost:3306/wayf
jdbc.maxActive=10
jdbc.maxIdle=5
jdbc.initialSize=5
jdbc.validationQuery=SELECT 1
 
redis.host=localhost
redis.port=6379

wayf.port=8080
wayf.domain=wayf-cloud-sandbox.literatumonline.com
```
The values may be overriden but all of the keys are required. Create this file in an easily accessible location.

### Build and Deploy Instructions
1. Checkout the desired branch from Github.
2. Open a command line prompt located in the top-level project directory where "pom.xml" is located
3. Execute the command `mvn package -DskipTests`. This will build a fat jar containing the service and all of its dependencies while skipping the test cases.
4. Start MySQL
    1. Determine which MySQL username and password you'd like to use
    2. Create the WAYF database: `mysql -h localhost -u [MYSQL_USER] -p[MYSQL_PASS] < src/test/resources/create_wayf_db.sql`
    3. Create the WAYF tables: `mysql -h localhost -u [MYSQL_USER] -p[MYSQL_PASS] wayf < src/test/resources/create_wayf_tables.sql`
5. Start Redis
6. Start the WAY application: `java -jar -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory target/wayf-cloud-1.0-SNAPSHOT-fat.jar -Dwayf.conf.dir=[PATH_TO_WAYF.PROPERTIES]`
    1. `vertx.logger-delegate-factory-class-name` enables the vert.x logging to work with WAYF cloud's logging system
    2. `wayf.conf.dir` tells the application where to load the wayf environment configuration from. If no value is specified, the application will attempt to load it from the classpath
