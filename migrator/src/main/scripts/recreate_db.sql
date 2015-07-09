drop database if exists ups_master;
create database ups_master default character set = "UTF8" default collate = "utf8_general_ci";
GRANT SELECT,INSERT,UPDATE,ALTER,DELETE,CREATE,DROP,INDEX ON ups_master.* TO 'unifiedpush'@'localhost';