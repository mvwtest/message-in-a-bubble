FROM ubuntu:latest

ENV OPENFIRE_VERSION=4.6.7 \
    JRE_VERSION=8

RUN apt-get update \
    && apt-get install -y wget systemctl openjdk-${JRE_VERSION}-jre \
    && wget https://www.igniterealtime.org/downloadServlet?filename=openfire/openfire_${OPENFIRE_VERSION}_all.deb -O /tmp/openfire_${OPENFIRE_VERSION}.deb \
    && apt-get install -y /tmp/openfire_${OPENFIRE_VERSION}.deb

CMD [ "systemctl", "start", "openfire" ]
