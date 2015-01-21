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
  certificate BLOB         NOT NULL,
  passphrase  VARCHAR(255) NOT NULL,
  production  BOOLEAN      NOT NULL,
  id          VARCHAR(255) NOT NULL,
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
  enabled         BOOLEAN       NOT NULL,
  operatingSystem VARCHAR(255)  DEFAULT NULL,
  osVersion       VARCHAR(255)  DEFAULT NULL,
  platform        VARCHAR(255)  DEFAULT NULL,
  variantID       VARCHAR(255)  DEFAULT NULL,
  PRIMARY KEY (id)
);