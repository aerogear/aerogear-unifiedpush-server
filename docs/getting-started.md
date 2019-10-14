# Getting started 

The AeroGear UnifiedPush Server provides a Docker image to running locally (Standalone)

```bash
docker run -p 9999:8080 -it aerogear/unifiedpush-configurable-container:latest
```

To access this server go to [http://localhost:9999/](http://localhost:9999/).

?> ℹ️ The standalone is going to use H2 as database, AMQ provides for the Wildfly using to run the UPS and it will not use the Keycloak.

// TODO This should be better explain

?> ℹ️ If you would like to use [docker-Compose](https://github.com/aerogear/aerogear-unifiedpush-server/tree/master/docker-compose).
