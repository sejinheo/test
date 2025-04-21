FROM openjdk:17-slim

# 필수 패키지 및 도구 설치 (기존 gradle 제거하지 않아도 됨)
RUN apt-get update && apt-get install -y ffmpeg wget unzip

# 최신 Gradle 설치 (8.12.1)
RUN wget https://services.gradle.org/distributions/gradle-8.12.1-bin.zip -P /tmp \
    && unzip -d /opt/gradle /tmp/gradle-8.12.1-bin.zip \
    && ln -s /opt/gradle/gradle-8.12.1/bin/gradle /usr/bin/gradle

# 앱 디렉토리 설정
WORKDIR /app

# 소스 코드 복사
COPY . .

# 빌드 실행
RUN gradle clean build --no-daemon

# 실행 경로 이동
WORKDIR /app/build/libs

# 서버 포트 오픈
EXPOSE 8080

# 실행 명령
CMD ["java", "-jar", "naebom-0.0.1-SNAPSHOT.jar"]