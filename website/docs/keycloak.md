---
id: keycloak
title: Keycloak Configuration
sidebar_label: Keycloak Configuration
---

## TODO  Document how the kc realm needs to be configured.

## Realm
Name : Aerogear

## Clients
 * unified-push-server
  * bearer-only
 * unified-push-server-js
   * "roles": ["admin", "developer"]
   * web origin: * 
   * redirect : *

## Roles
 * admin (sees all applications)
 * developer (only sees applications made by that account)
