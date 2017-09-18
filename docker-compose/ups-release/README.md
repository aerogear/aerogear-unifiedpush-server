# UnifiedPush Server - Docker Compose

The quickest way to run the latest version of the UPS is running it [Docker image](https://github.com/jboss-dockerfiles/aerogear/tree/master/wildfly/unifiedpush-wildfly).

But this still requires a few commands, to launch the required DB and the server itself. To fasten the process `cd` into this folder and use our Docker Compose files:

| Compose File Version    	| Docker Engine Version 	|
|-------------------------	|-----------------------	|
| docker-compose-v2.1.yaml 	| 1.12.0+               	|


```
docker-compose -f docker-compose-v2.1.yaml up -d
```

This fires up all the components you need and finally launches the UPS at: `http:DOCKER_IP:9999/ag-push`

**NOTE:** this requires that the `keycloak` IP address needs to be also added to your `/etc/hosts`

