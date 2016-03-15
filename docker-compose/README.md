#UnifiedPush Server - Docker Compose

The quickest way to run the latest version of the UPS is running it [Docker Dev image](https://github.com/jboss-dockerfiles/aerogear/tree/master/wildfly/unifiedpush-wildfly-dev#running-the-image).

But this still requires a few commands, to launch the required DB and the server itself. To fasten the process `cd` into this folder and use our Docker Compose file:

```
docker-compose up -d
```

This fires up all the components you need and finally launches the UPS at: `https:DOCKER_IP:8443/ag-push`
