--
-- PGSQL database query
--

INSERT INTO push_application VALUES ('711c8b98-9c2c-4d94-abe7-ecff7145c015', NULL, 'admin', '1f73558f-d01b-48d7-9f6a-fc5d0ec6da51', 'DEFAULT', '7385c294-2003-4abf-83f6-29a27415326b');

INSERT INTO variant VALUES ('ios', '5ede4a1b-0277-4467-992c-02a7b307e555', NULL, 'admin', 'DEFAULT', '088a814a-ff2b-4acf-9091-5bcd0ccece16', 1, 'd3f54c25-c3ce-4999-b7a8-27dc9bb01364', '711c8b98-9c2c-4d94-abe7-ecff7145c015');
INSERT INTO variant VALUES ('android', 'da168ba1-105a-4ba5-a17f-46c7ea2a32e4', NULL, 'admin', 'DEFAULT', '20aba35f-e472-4958-9f8f-f55f73e2e012', 0, '55b2c428-7102-43f7-96c0-96b5c7ba8dcc', '711c8b98-9c2c-4d94-abe7-ecff7145c015');

INSERT INTO ios_variant VALUES ('DEFAULT', 'DEFAULT', false, '5ede4a1b-0277-4467-992c-02a7b307e555');
INSERT INTO android_variant VALUES ('DEFAULT', 'DEFAULT', 'da168ba1-105a-4ba5-a17f-46c7ea2a32e4');
