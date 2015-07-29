drop database if exists ups_stable;
create database ups_stable default character set = "UTF8" default collate = "utf8_general_ci";
GRANT SELECT,INSERT,UPDATE,ALTER,DELETE,CREATE,DROP,INDEX ON ups_stable.* TO 'unifiedpush'@'localhost';