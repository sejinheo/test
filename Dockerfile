FROM openjdk:17
RUN apt-get update && apt-get install -y ffmpeg
WORKDIR /app
COPY build/libs/naebom-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
