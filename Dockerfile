FROM openjdk:17

RUN apt-get update && apt-get install -y ffmpeg gradle

WORKDIR /app
COPY . .

RUN gradle clean build --no-daemon

WORKDIR /app/build/libs
EXPOSE 8080
CMD ["java", "-jar", "naebom-0.0.1-SNAPSHOT.jar"]
