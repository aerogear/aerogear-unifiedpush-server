--
-- Migrate KC refactorings done between 1.0-beta-4 and 1.0.Final
--
alter table REALM_AUDIT_LISTENERS rename to REALM_EVENTS_LISTENERS;
alter table REALM rename column AUDIT_ENABLED to EVENTS_ENABLED;
alter table REALM rename column AUDIT_EXPIRATION to EVENTS_EXPIRATION;
