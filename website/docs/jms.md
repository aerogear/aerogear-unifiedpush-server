---
id: jms
title: JMS Broker Configuration
sidebar_label: JMS Broker Configuration
---

## Usage of JMS in Unified Push
Unified Push uses JMS to queue work and update configuration for senders.  By default, Unified Push uses a JMS broker build into its Wildfly Applicaiton Server, but it may also be configured to use a service which implements JMS with the AMQP protocol such as [Apache Artemis](https://activemq.apache.org/components/artemis/).  If you wish to run Unified Push in a multi-instance environment, you will need to configure an external AMQP server to coordinate work among your Unified Push instances.

## Unified Push Address Definitions

Unified Push has several queues and addresses that it expects to be available.  Each push service has two queues named in the format  *${PushServiceName}TokenBatchQueue* and *${PushServiceName}PushMessageQueue*.  These queues schedule Unified Push to lookup metadata for a request to send a push message and to send formatted messages to the respective push services respectively.  Additionally there are queues and addresses which signal configuration changes, metrics updates, and other internal Unified Push service events.

### Push Service queues

The following is a list of queues used by Unified Push to send messages:

Queue Name | Description|
-----------|------------|
GCMPushMessageQueue| This Queue triggers work to send messages to FCM.  |
GCMTokenBatchQueue|This Queue triggers work to prepare messages to FCM.  |
WebPushMessageQueue|This Queue triggers work to send messages to web push.|
WebTokenBatchQueue|This Queue triggers work to prepare messages to web push.  |
APNsPushMessageQueue|This Queue triggers work to send messages to APNs. |
APNsTokenBatchQueue|This Queue triggers work to prepare messages to APNs.  |
WNSPushMessageQueue|*deprecated* This queue will be removed in a future release.|
WNSTokenBatchConsumer|*deprecated*  This queue will be removed in a future release.|

### Unified Push Service internal topics

Additionally, there is a topic which is used by Unified Push to restart connections to APNs.
Queue Name | Description|
-----------|------------|
APNSClient| This signals [APNSClientConsumer](https://github.com/aerogear/aerogear-unifiedpush-server/blob/master/push-sender/src/main/java/org/jboss/aerogear/unifiedpush/message/jms/APNSClientConsumer.java) to reload its configuration and reconnect to APNs.|

## External AMQP Broker Configuration 
Unified Push has been tested with EnMasse, AMQ Online, and AMQ Artemis as JMS brokers.  In theory it should work any AMQP broker that supports JMS.  Unified Push's support for external brokers can be enabled and configured by setting the following environment variables at startup:

Name|Description|
----|-----------|
ARTEMIS_USER|A username to connect to an AMQP server|
ARTEMIS_PASSWORD|A password to connect to an AMQP server|
ARTEMIS_SERVICE_HOST|AMQP server hostname or ip address|
ARTEMIS_SERVICE_PORT|AMQP server port|
AMQ_MAX_RETRIES|'optional' Number of times to retry sending a push message before discarding the JMS message. <br>*Default 3*|
AMQ_BACKOFF_SECONDS|'optional' Number of seconds to delay retrying a JMS message. <br>*Default 10*|

### Artemis Example with docker-compose

We provide an example configuration using docker-compose in our [GitHub repository](https://github.com/aerogear/aerogear-unifiedpush-server/tree/master/docker-compose).

### Enmasse Example with an Operator

If you manage Unified Push using the Unified Push operator, you may set `useMessageBroker` to true in your UnifiedPushServer CR.  See [our example in the unifiedpush-operator repository](https://github.com/aerogear/unifiedpush-operator/blob/master/deploy/crds/push_v1alpha1_unifiedpushserver_cr_with_enmasse.yaml)