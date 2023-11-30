FROM sbtscala/scala-sbt:eclipse-temurin-jammy-8u352-b08_1.9.4_2.13.11

COPY . /app
# Set the working directory in the container
WORKDIR /app
# Define environment variable
ENV JAVA_OPTS=""
# The CMD command can be overridden by docker-compose.yml
CMD ["sbt", "run outputs/"]