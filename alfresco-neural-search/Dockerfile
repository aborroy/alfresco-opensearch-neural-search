FROM alfresco/alfresco-base-java:jre17-rockylinux8-202302221525

ENV PORT 8081
EXPOSE 8081
ARG JAR_FILE=target/neural-search-*.jar

WORKDIR /opt/app

RUN groupadd -r neural && useradd -r -g neural neural

COPY ${JAR_FILE} app.jar
RUN chown neural:neural app.jar && chmod 400 app.jar

USER neural

ENTRYPOINT ["java", "-jar", "app.jar"]
