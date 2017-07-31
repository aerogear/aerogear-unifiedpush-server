# UnifiedPush Server - Docker Compose

The quickest way to run the latest version of the UPS is running it [Docker Dev image](https://github.com/jboss-dockerfiles/aerogear/tree/master/wildfly/unifiedpush-wildfly-dev#running-the-image).

But this still requires a few commands, to launch the required DB and the server itself. To fasten the process `cd` into this folder and use our Docker Compose files:

| Compose File Version    	| Docker Engine Version 	|
|-------------------------	|-----------------------	|
| docker-compose-v1.yaml  	| 1.9.1.+               	|
| docker-compose-v3x.yaml 	| 1.13.0+               	|


```
docker-compose -f docker-compose-v1.yaml up -d
```

This fires up all the components you need and finally launches the UPS at: `https:DOCKER_IP:8443/ag-push`
