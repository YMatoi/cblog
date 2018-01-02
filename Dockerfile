FROM clojure:alpine AS build-env
ADD . /work
WORKDIR /work
RUN lein cljsbuild once
RUN lein uberjar

FROM openjdk:8-jre-alpine
COPY --from=build-env /work/target/cblog-0.1.0-SNAPSHOT-standalone.jar /opt/main.jar
CMD ["java","-jar","/opt/main.jar"]
