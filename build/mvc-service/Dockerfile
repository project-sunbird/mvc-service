FROM sunbird/openjdk-java11-alpine 
RUN apk update \
    && apk add  unzip \
    && apk add curl \
    && adduser -u 1001 -h /home/sunbird/ -D sunbird \
    && mkdir -p /home/sunbird
RUN chown -R sunbird:sunbird /home/sunbird
USER sunbird
COPY ./mvc-api/mvc-service/target/mvc-service-1.0-SNAPSHOT-dist.zip /home/sunbird/
RUN unzip /home/sunbird/mvc-service-1.0-SNAPSHOT-dist.zip -d /home/sunbird/
RUN rm /home/sunbird/mvc-service-1.0-SNAPSHOT-dist.zip
COPY --chown=sunbird ./schemas /home/sunbird/mvc-service-1.0-SNAPSHOT/schemas
WORKDIR /home/sunbird/
CMD java -XX:+PrintFlagsFinal $JAVA_OPTIONS  -cp '/home/sunbird/mvc-service-1.0-SNAPSHOT/lib/*' -Dconfig.file=/home/sunbird/mvc-service-1.0-SNAPSHOT/config/application.conf -Dlogger.file=/home/sunbird/mvc-service-1.0-SNAPSHOT/config/logback.xml play.core.server.ProdServerStart /home/sunbird/mvc-service-1.0-SNAPSHOT
