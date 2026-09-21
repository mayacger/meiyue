# 美月商城 API 生产镜像（I34）
# 多阶段：Maven 打包 → JRE 运行
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY meiyue-common ./meiyue-common
COPY meiyue-identity ./meiyue-identity
COPY meiyue-tenant ./meiyue-tenant
COPY meiyue-catalog ./meiyue-catalog
COPY meiyue-decoration ./meiyue-decoration
COPY meiyue-trade ./meiyue-trade
COPY meiyue-payment ./meiyue-payment
COPY meiyue-logistics ./meiyue-logistics
COPY meiyue-aftersale ./meiyue-aftersale
COPY meiyue-ai-assist ./meiyue-ai-assist
COPY meiyue-support ./meiyue-support
COPY meiyue-platform ./meiyue-platform
COPY meiyue-boot ./meiyue-boot
COPY meiyue-worker ./meiyue-worker
RUN mvn -q -DskipTests -pl meiyue-boot -am package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN adduser -D -u 10001 meiyue
COPY --from=build /src/meiyue-boot/target/meiyue-boot-*.jar /app/app.jar
USER meiyue
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
HEALTHCHECK --interval=15s --timeout=5s --retries=5 \
  CMD wget -qO- http://127.0.0.1:8080/actuator/health | grep -q UP || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
