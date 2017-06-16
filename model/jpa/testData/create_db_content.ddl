CREATE TABLE push_application (
  id                VARCHAR(255) NOT NULL,
  description       VARCHAR(255),
  developer         VARCHAR(255),
  master_secret      VARCHAR(255),
  name              VARCHAR(255) NOT NULL,
  api_key VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE android_variant (
  google_key     VARCHAR(255) NOT NULL,
  project_number VARCHAR(255) DEFAULT NULL,
  id            VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE ios_variant (
  cert_data  VARCHAR(1000) NOT NULL,
  passphrase VARCHAR(255)  NOT NULL,
  production BOOLEAN       NOT NULL,
  id         VARCHAR(255)  NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE simple_push_variant (
  id VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE variant (
  VARIANT_TYPE VARCHAR(31)  NOT NULL,
  id           VARCHAR(255) NOT NULL,
  description  VARCHAR(255) DEFAULT NULL,
  developer    VARCHAR(255) DEFAULT NULL,
  name         VARCHAR(255) DEFAULT NULL,
  secret       VARCHAR(255) DEFAULT NULL,
  type         INT          DEFAULT NULL,
  api_key    VARCHAR(255) DEFAULT NULL,
  push_application_id  VARCHAR(255) DEFAULT NULL,
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

CREATE TABLE push_message_info (
  id                VARCHAR(255) NOT NULL,
  client_identifier  VARCHAR(255)  DEFAULT NULL,
  ip_address         VARCHAR(255)  DEFAULT NULL,
  push_application_id VARCHAR(255) NOT NULL,
  raw_json_message    VARCHAR(4500) DEFAULT NULL,
  submit_date        DATE          DEFAULT NULL,
  total_receivers    BIGINT       NOT NULL,
  app_open_counter    BIGINT      DEFAULT 0,
  first_open_date     DATE          DEFAULT NULL,
  last_open_date      DATE          DEFAULT NULL,
  served_variants    BIGINT       DEFAULT 0,
  total_variants    BIGINT      DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE TABLE flat_push_message_info (
  id                VARCHAR(255) NOT NULL,
  push_application_id VARCHAR(255) NOT NULL,
  raw_json_message    VARCHAR(4500) DEFAULT NULL,
  ip_address         VARCHAR(255)  DEFAULT NULL,
  client_identifier  VARCHAR(255)  DEFAULT NULL,
  submit_date        DATE          DEFAULT NULL,
  app_open_counter    BIGINT      DEFAULT 0,
  first_open_date     DATE          DEFAULT NULL,
  last_open_date      DATE          DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE variant_error_status (
  push_message_variant_id VARCHAR(255) NOT NULL,
  error_reason VARCHAR(255) NOT NULL,
  variant_id VARCHAR(255)  NOT NULL REFERENCES variant (id),
  push_job_id VARCHAR(255) NOT NULL REFERENCES flat_push_message_info (id),
  PRIMARY KEY (push_message_variant_id)
);

CREATE TABLE variant_metric_info (
  id                     VARCHAR(255) NOT NULL,
  delivery_status         BOOLEAN      DEFAULT NULL,
  reason                 VARCHAR(255) DEFAULT NULL,
  receivers              BIGINT       NOT NULL,
  variant_id              VARCHAR(255) NOT NULL,
  push_message_info_id VARCHAR(255) DEFAULT NULL,
  variant_open_counter    BIGINT  DEFAULT 0,
  served_batches    BIGINT       DEFAULT 0,
  total_batches    BIGINT      DEFAULT 0,
  PRIMARY KEY (id)
);
