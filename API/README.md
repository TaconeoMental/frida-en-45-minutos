# API

## Deployment
Armar imagen de docker
```bash
docker build -t frida_api .
`````

Levantar el servidor
```bash
docker run -d -p8080:8080 --name frida_main frida_api
`````
