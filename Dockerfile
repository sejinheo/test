FROM openjdk:17-slim

# 필수 패키지 설치
RUN apt-get update && apt-get install -y ffmpeg gradle

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
