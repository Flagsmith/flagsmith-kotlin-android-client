# Runs this SDK, built from source, under the Flagsmith SDK behavioural spec.
# docker build -f sdk-spec/Dockerfile --build-context harness=<sdk-spec>/harness/kotlin .
FROM docker.io/library/gradle:8.10.2-jdk17 AS build

WORKDIR /sdk
COPY sdk-spec/settings.gradle.kts sdk-spec/build.gradle.kts ./sdk-spec/
RUN gradle --no-daemon --quiet -p sdk-spec dependencies > /dev/null

COPY FlagsmithClient/src/main ./FlagsmithClient/src/main
COPY sdk-spec/src ./sdk-spec/src
COPY sdk-spec/steps.kt ./sdk-spec/steps.kt
COPY --from=harness Harness.kt /harness/Harness.kt
RUN gradle --no-daemon --quiet -p sdk-spec installDist -Pharness=/harness

FROM docker.io/library/eclipse-temurin:17-jre

WORKDIR /sdk
COPY --from=build /sdk/sdk-spec/build/install/sdk-spec ./app
COPY sdk-spec/steps.kt ./sdk-spec/steps.kt

ENTRYPOINT ["app/bin/sdk-spec", "sdk-spec/steps.kt"]
