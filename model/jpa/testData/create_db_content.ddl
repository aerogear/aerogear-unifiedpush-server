CREATE TABLE PushApplication (
  id                VARCHAR(255) NOT NULL,
  description       VARCHAR(255),
  developer         VARCHAR(255),
  masterSecret      VARCHAR(255),
  name              VARCHAR(255) NOT NULL,
  pushApplicationID VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE AndroidVariant (
  googleKey     VARCHAR(255) NOT NULL,
  projectNumber VARCHAR(255) DEFAULT NULL,
  id            VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE iOSVariant (
  cert_data  VARCHAR(1000) NOT NULL,
  passphrase VARCHAR(255)  NOT NULL,
  production BOOLEAN       NOT NULL,
  id         VARCHAR(255)  NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE SimplePushVariant (
  id VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Variant (
  VARIANT_TYPE VARCHAR(31)  NOT NULL,
  id           VARCHAR(255) NOT NULL,
  description  VARCHAR(255) DEFAULT NULL,
  developer    VARCHAR(255) DEFAULT NULL,
  name         VARCHAR(255) DEFAULT NULL,
  secret       VARCHAR(255) DEFAULT NULL,
  type         INT          DEFAULT NULL,
  variantID    VARCHAR(255) DEFAULT NULL,
  variants_id  VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Installation (
  id              VARCHAR(255) NOT NULL,
  alias           VARCHAR(255)  DEFAULT NULL,
  deviceToken     VARCHAR(4096) DEFAULT NULL,
  deviceType      VARCHAR(255)  DEFAULT NULL,
  enabled         BOOLEAN      NOT NULL,
  operatingSystem VARCHAR(255)  DEFAULT NULL,
  osVersion       VARCHAR(255)  DEFAULT NULL,
  platform        VARCHAR(255)  DEFAULT NULL,
  variantID       VARCHAR(255)  DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Category (
  id   BIGINT NOT NULL,
  name VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Installation_Category (
  Installation_id VARCHAR(255) NOT NULL,
  categories_id   BIGINT       NOT NULL,
  PRIMARY KEY (Installation_id, categories_id)
);

CREATE TABLE PushMessageInformation (
  id                VARCHAR(255) NOT NULL,
  clientIdentifier  VARCHAR(255)  DEFAULT NULL,
  ipAddress         VARCHAR(255)  DEFAULT NULL,
  pushApplicationId VARCHAR(255) NOT NULL,
  rawJsonMessage    VARCHAR(4500) DEFAULT NULL,
  submitDate        DATE          DEFAULT NULL,
  totalReceivers    BIGINT       NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE VariantMetricInformation (
  id                     VARCHAR(255) NOT NULL,
  deliveryStatus         BOOLEAN      DEFAULT NULL,
  reason                 VARCHAR(255) DEFAULT NULL,
  receivers              BIGINT       NOT NULL,
  variantID              VARCHAR(255) NOT NULL,
  variantInformations_id VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (id)
);