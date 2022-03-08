FROM adoptopenjdk/openjdk11:alpine-jre
ADD build/distributions/server-1.0-SNAPSHOT.tar /home
ENTRYPOINT ["/bin/sh", "/home/server-1.0-SNAPSHOT/bin/server"]