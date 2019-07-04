# UnifiedPush Server - Docker Compose

The quickest way to run the latest release of the UPS is running it [Docker image](https://hub.docker.com/r/aerogear/).

But this still requires a few commands, to launch the required DB and the server itself. To fasten the process `cd` into this folder and use our Docker Compose files:

```
docker-compose up
```

This fires up all the components you need and finally launches the UPS at: `http:DOCKER_IP:9999/`

There is a pre configured admin user on Keycloak instance with the 123 password.

> There is a *bug* on *docker-compose for Mac* with does not expose internal docker IP. To fix it you will need:

1. Change the keycloak service host to your machine IP [here](https://github.com/aerogear/aerogear-unifiedpush-server/blob/master/docker-compose/helper/exportKeycloakHost.sh#L6)
   ```shell
   export KEYCLOAK_SERVICE_HOST=[PUT YOUR MACHINE IP HERE] # change for you machine IP
   ``` 