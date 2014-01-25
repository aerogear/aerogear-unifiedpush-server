#!/bin/sh

SECRET_KEY=`openssl rand -rand /dev/urandom -hex 64`
echo "secret_key=$SECRET_KEY" > src/main/resources/config.properties
