# UnifiedPush Server - Docker Compose

## Official Release with database and Keycloak

The quickest way to run the latest release of the UPS is running it [Docker image](https://github.com/jboss-dockerfiles/aerogear/tree/master/wildfly/unifiedpush-wildfly).

But this still requires a few commands, to launch the required DB and the server itself. To fasten the process `cd` into this folder and use our Docker Compose files:

| Compose File Version    	| Docker Engine Version 	|
|-------------------------	|-----------------------	|
| docker-compose-v2.1.yaml 	| 1.12.0+               	|


```
docker-compose -f docker-compose-v2.1.yaml up -d
```

This fires up all the components you need and finally launches the UPS at: `http:DOCKER_IP:9999/ag-push`

## Development mode

in the `servers` directory we have two different flavors of the UPS:
* `plain`: Just UPS, with H2 in-memory database
* `keycloak`: UPS and Keycloak, both using H2 in-memory database

### Plain UPS

During the build of the project the `aerogear/ups:plain` is build. Afterwards, simply run it like:

```
docker run -p 18081:8080 -it aerogear/ups:plain 
```

This brings the UPS containers. Now, go to `http://localhost:18081/` and you can use it right away!


### Keycloak UPS 

During the build of the project the `aerogear/ups:kc` is build. Afterwards, simply run it like:

```
docker run -p 8080:8080 -it aerogear/ups:kc
```

This brings up both, Keycloak and the UPS containers. Now, go to `http://localhost:8080/` to login to the UPS!
