#
# Docker nodejs/npm container
#
FROM node:6
MAINTAINER Inaki Anduaga <inaki@inakianduaga.com>

WORKDIR /app
VOLUME /app

EXPOSE 3000

ENTRYPOINT ["npm"]
CMD ["run"]

# enable color in terminal
ENV TERM=xterm-256color
