# Java 17 + FFmpeg 설치
FROM ubuntu:22.04

# Java, FFmpeg 설치
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    ffmpeg \
    curl \
    unzip

# 작업 디렉토리 설정
WORKDIR /app

# JAR 파일 복사
COPY build/libs/naebom-0.0.1-SNAPSHOT.jar app.jar

# 포트 설정
EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]