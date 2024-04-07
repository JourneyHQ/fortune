FROM azul/zulu-openjdk-alpine:17-latest as builder

RUN apk add --no-cache git wget unzip

WORKDIR /build

COPY gradle gradle

COPY gradlew build.gradle.kts gradle.properties settings.gradle.kts ./

RUN chmod a+x gradlew
RUN ./gradlew build || return 0

COPY src src

RUN ./gradlew build

FROM azul/zulu-openjdk-alpine:17-latest as runner

WORKDIR /app

# hadolint ignore=DL3018
RUN apk add --update --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Tokyo /etc/localtime && \
    echo "Asia/Tokyo" > /etc/timezone && \
    apk del tzdata

COPY --from=builder /build/build/libs/vcspeaker-*.jar /app/vcspeaker-kt.jar

ENV TZ Asia/Tokyo

CMD ["java", "-jar", "/app/fortune.jar"]
