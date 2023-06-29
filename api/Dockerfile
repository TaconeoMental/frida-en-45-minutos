FROM debian:latest

# Puerto TCP de la aplicación web
EXPOSE 8080

# Usuario regular para correr la app dentro del contenedor
ARG UID=1000
ARG GID=1000

RUN groupadd -g "${GID}" webapp \
 && useradd --create-home --no-log-init -u "${UID}" -g "${GID}" webapp

# Dependencias básicas
RUN apt-get update && apt-get install -y \
 python3-pip \
 vim \
 curl \
 wget \
 bash \
 supervisor

# Todo va a correr en el contexto de un usuario sin privilegios, incluyendo supervisord
USER webapp

# Copiamos el código fuente. Es tarde y me da flojera chownear directorios ajenos ahora.
# Por ahora en el home de webapp
ENV WEBAPP_DIR /home/webapp/feccm
RUN mkdir -p $WEBAPP_DIR
WORKDIR $WEBAPP_DIR
COPY --chown=webapp:webapp ./app ./app
COPY ./requirements.txt .

# Python
# Es un docker, no me importa separar paquetes de sistema y de usuario
RUN python3 -m pip install -r requirements.txt --break-system-packages
ENV PATH="/home/webapp/.local/bin:${PATH}"

# Ahora se viene lo chido
COPY ./supervisord.conf /etc/supervisor/conf.d/supervisord.conf

CMD ["/usr/bin/supervisord"]
