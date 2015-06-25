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

CREATE TABLE installation (
  id              VARCHAR(255) NOT NULL,
  alias           VARCHAR(255)  DEFAULT NULL,
  device_token     VARCHAR(4096) DEFAULT NULL,
  device_type      VARCHAR(255)  DEFAULT NULL,
  enabled         BOOLEAN      NOT NULL,
  operating_system VARCHAR(255)  DEFAULT NULL,
  os_version       VARCHAR(255)  DEFAULT NULL,
  platform        VARCHAR(255)  DEFAULT NULL,
  variant_id       VARCHAR(255)  DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE category (
  id   BIGINT NOT NULL,
  name VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE installation_category (
  installation_id VARCHAR(255) NOT NULL,
  category_id   BIGINT       NOT NULL,
  PRIMARY KEY (installation_id, category_id)
);

CREATE TABLE PushMessageInformation (
  id                VARCHAR(255) NOT NULL,
  clientIdentifier  VARCHAR(255)  DEFAULT NULL,
  ipAddress         VARCHAR(255)  DEFAULT NULL,
  pushApplicationId VARCHAR(255) NOT NULL,
  rawJsonMessage    VARCHAR(4500) DEFAULT NULL,
  submitDate        DATE          DEFAULT NULL,
  totalReceivers    BIGINT       NOT NULL,
  appOpenCounter    BIGINT      DEFAULT 0,
  firstOpenDate     DATE          DEFAULT NULL,
  lastOpenDate      DATE          DEFAULT NULL,
  servedVariants    BIGINT       DEFAULT 0,
  totalVariants    BIGINT      DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE TABLE VariantMetricInformation (
  id                     VARCHAR(255) NOT NULL,
  deliveryStatus         BOOLEAN      DEFAULT NULL,
  reason                 VARCHAR(255) DEFAULT NULL,
  receivers              BIGINT       NOT NULL,
  variantID              VARCHAR(255) NOT NULL,
  pushMessageInformation_id VARCHAR(255) DEFAULT NULL,
  variantOpenCounter    BIGINT  DEFAULT 0,
  servedBatches    BIGINT       DEFAULT 0,
  totalBatches    BIGINT      DEFAULT 0,
  PRIMARY KEY (id)
);