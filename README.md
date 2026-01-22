# Spring Boot + React Application

In this repository, we serve `2` Single Page Applications using a Spring Boot Backend:
1. React Application
2. Vue.js application

But, the approach works for any kind of front-end framework.

## Objective

Create a Spring Boot Application, which serves one or more Single Page Applications.
We want to address the following requirements:
1. Serving one or more SPAs using a single Spring Boot Application ✅
2. Routing should work as expected ✅
3. Protecting SPA using Spring Security ✅
4. Adding different security configuration for different SPAs ✅
5. Accessing backend resources from SPA ✅
6. Configuring CSRF for SPAs ✅
7. Enabling rapid development in watch mode ✅

## Building the App

Create a `jar` file using the following command:

```shell
./gradlew bootJar
```

This will generate a fat jar in `build/libs` directory. Run the `jar` file using the following command:

```shell
java -jar /path/to/file.jar
```

This will start the application on port `8080`. You can configure port and other things, since it is a normal spring boot application.

## Development Mode

While developing, you may want to build the web apps in watch mode, so that you don't need to restart the spring app after each file change. In separate terminals, run the following `2` commands (for `2` apps) to build them in watch mode:

```shell
./gradlew :app1:watchWebapp

./gradlew :app2:watchWebapp
```

Then start the spring boot application with `local` active profile. This will make spring load the configurations from `src/main/resources/application-loca.properties` file, which disables the cache for spring assets and thymeleaf (so that you don't need to restart the spring application for the web app changes to take affect).

## Creating Docker Image

In order to create docker image of the project, run the following command:

```shell
./gradlew bootBuildImage
```

In order to run the app in docker (from the image created using the above command), run the following command:

```shell
docker run --expose 8080 --publish 8080:8080 <image-name>
```

NOTE: You can see the available images in your system using the following command:

```shell
docker image ls # docker images or docker image list
```
