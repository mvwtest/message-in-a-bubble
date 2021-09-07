FROM ubuntu:latest

ENV OPENFIRE_VERSION=4.6.4 \
    JRE_VERSION=8

RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get install -y wget systemctl openjdk-${JRE_VERSION}-jre \
    && wget https://www.igniterealtime.org/downloadServlet?filename=openfire/openfire_${OPENFIRE_VERSION}_all.deb -O /tmp/openfire_${OPENFIRE_VERSION}.deb \
    && apt-get install -y /tmp/openfire_${OPENFIRE_VERSION}.deb

CMD [ "/bin/systemctl", "start", "openfire" ]
EXPOSE 5222/tcp 5223/tcp 7070/tcp 7443/tcp 5269/tcp 5270/tcp 5275/tcp 5276/tcp 5262/tcp 5263/tcp 9090/tcp 9091/tcp 7777/tcp 5229/tcp