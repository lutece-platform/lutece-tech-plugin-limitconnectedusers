-- liquibase formatted sql
-- changeset limitconnectedusers:init_core_limitconnectedusers.sql
-- validCheckSum: 8:1ff3e9f3ea3700808a1de08eb542adc8
-- validCheckSum: 9:ca96d086b9c6b39d939d7b12acbf2f24
-- preconditions onFail:MARK_RAN onError:WARN
INSERT INTO core_datastore ( entity_key, entity_value ) VALUES ('limitconnectedusers.site_property.limit_message.textblock', '<div class=''alert alert-danger''>Le nombre maximal d''utilisateur connecté simultanément a été atteint</div>');
INSERT INTO core_datastore ( entity_key, entity_value ) VALUES ('limitconnectedusers.site_property.limit_notification_mailing_list.textblock', '');
INSERT INTO core_datastore ( entity_key, entity_value ) VALUES ('limitconnectedusers.site_property.limit_notification_message.textblock', 'Le nombre maximal d''utilisateur connecté simultanément a été atteint');
INSERT INTO core_datastore ( entity_key, entity_value ) VALUES ('limitconnectedusers.site_property.limit_notification_sender_name', 'LUTECE');
INSERT INTO core_datastore ( entity_key, entity_value ) VALUES ('limitconnectedusers.site_property.limit_notification_subject.textblock','Le nombre maximal d''utilisateur connecté simultanément a été atteint');



