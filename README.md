# Vert.x 3.0.0 Chat Application
Example Vert.x 3.0 EventBus Bridge Application

## Overview
This application is written using the forthcoming Vert.x 3.0.0 release (still 
in Alpha stage). This application uses the vertx-apex web extensions to make a 
simple websocket/eventbus driven chat application for the web.

## Prerequisites

- Java 8
- Maven

## Building

The application requires the current SNAPSHOT version of Vert.x 3.0.0, so you
will need to configure your Maven settings.xml to pull those dependencies from
the Sonatype OSS Snapshots repository. 

~/.m2/settings.xml
```xml
<settings>
 ....
<profiles>
<profile>
  <id>sonatypeoss</id>
  <repositories>
    <repository>
      <id>sonatype-oss-snapshots</id>
      <name>Sonatype OSS Snapshots</name>
      <url>https://oss.sonatype.org/content/repositories/snapshots</url>
     </repository>
    </repositories>
 </profile>
</profiles>
<activeProfiles>
  <activeProfile>sonatypeoss</activeProfile>
</activeProfiles>
</settings>
```

Then, clone the repository:

```bash
git clone git@github.com:InfoSec812/distributed-chat-service.git
```

Finally, build and run:

```bash
cd distributed-chat-service
mvn clean compile
mvn exec:java -Dexec.mainClass=com.zanclus.distributed.chat.service.Main
```

The application will be started and listen on all local interfaces on port 8000.

http://localhost:8000/


