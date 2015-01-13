use unifiedpush;

--
-- Migrate KC refactorings done between 1.0-beta-4 and 1.0.Final
--
rename table REALM_AUDIT_LISTENERS to REALM_EVENTS_LISTENERS;
alter table REALM change AUDIT_ENABLED EVENTS_ENABLED bit(1);
alter table REALM change AUDIT_EXPIRATION EVENTS_EXPIRATION bigint(20);
