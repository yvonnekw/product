FROM openjdk:23
VOLUME /tmp
COPY target/product.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]