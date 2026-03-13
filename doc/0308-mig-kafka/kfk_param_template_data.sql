-- ==============================================================================
-- Initial Data for KFK_PARAM_TEMPLATE
-- Description: Default parameters for Kafka JDBC Source/Sink Connectors
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- Level 0 (Connector Selection)
-- ------------------------------------------------------------------------------
INSERT INTO KFK_PARAM_TEMPLATE (id, param_name, input_method, param_explain, param_key, par_class_id, dp_level, dp_order, hidden_yn, group_cd, par_param_id, par_column_key, column_type, required_yn, default_value) 
VALUES ('C-L0-001', 'Source Connector', 'COMBO', 'Source DB extraction connector', 'source_connector', NULL, 0, 10, 'N', 'KFK-0041', NULL, NULL, 'string', 'Y', 'io.confluent.connect.jdbc.JdbcSourceConnector');

INSERT INTO KFK_PARAM_TEMPLATE (id, param_name, input_method, param_explain, param_key, par_class_id, dp_level, dp_order, hidden_yn, group_cd, par_param_id, par_column_key, column_type, required_yn, default_value) 
VALUES ('C-L0-002', 'Sink Connector', 'COMBO', 'Target DB load connector', 'sink_connector', NULL, 0, 20, 'N', 'KFK-0042', NULL, NULL, 'string', 'Y', 'io.confluent.connect.jdbc.JdbcSinkConnector');

-- ------------------------------------------------------------------------------
-- JDBC Source Connector Specific Parameters (Level 1)
-- ------------------------------------------------------------------------------
INSERT INTO KFK_PARAM_TEMPLATE (id, param_name, input_method, param_explain, param_key, par_class_id, dp_level, dp_order, hidden_yn, group_cd, par_param_id, par_column_key, column_type, required_yn, default_value) 
VALUES ('PT-L1-001', 'Topic Prefix', 'TEXT', 'Prefix for Kafka topics', 'topic.prefix', 'io.confluent.connect.jdbc.JdbcSourceConnector', 1, 10, 'N', NULL, NULL, NULL, 'string', 'Y', 'mysql-01-');

INSERT INTO KFK_PARAM_TEMPLATE (id, param_name, input_method, param_explain, param_key, par_class_id, dp_level, dp_order, hidden_yn, group_cd, par_param_id, par_column_key, column_type, required_yn, default_value) 
VALUES ('PT-L1-002', 'Connection URL', 'AUTO', 'JDBC URL for Source DB', 'connection.url', 'io.confluent.connect.jdbc.JdbcSourceConnector', 1, 20, 'Y', NULL, NULL, NULL, 'string', 'Y', '');

INSERT INTO KFK_PARAM_TEMPLATE (id, param_name, input_method, param_explain, param_key, par_class_id, dp_level, dp_order, hidden_yn, group_cd, par_param_id, par_column_key, column_type, required_yn, default_value) 
VALUES ('PT-L1-003', 'Connection User', 'AUTO', 'DB Username', 'connection.user', 'io.confluent.connect.jdbc.JdbcSourceConnector', 1, 30, 'Y', NULL, NULL, NULL, 'string', 'Y', '');

INSERT INTO KFK_PARAM_TEMPLATE (id, param_name, input_method, param_explain, param_key, par_class_id, dp_level, dp_order, hidden_yn, group_cd, par_param_id, par_column_key, column_type, required_yn, default_value) 
VALUES ('PT-L1-004', 'Connection Password', 'AUTO', 'DB Password', 'connection.password', 'io.confluent.connect.jdbc.JdbcSourceConnector', 1, 40, 'Y', NULL, NULL, NULL, 'string', 'Y', '');

-- ------------------------------------------------------------------------------
-- JDBC Source Connector Specific Parameters (Level 2)
-- ------------------------------------------------------------------------------
INSERT INTO KFK_PARAM_TEMPLATE (id, param_name, input_method, param_explain, param_key, par_class_id, dp_level, dp_order, hidden_yn, group_cd, par_param_id, par_column_key, column_type, required_yn, default_value) 
VALUES ('PT-L2-001', 'Table Whitelist', 'TEXT', 'Comma-separated list of tables to include', 'table.whitelist', 'io.confluent.connect.jdbc.JdbcSourceConnector', 2, 10, 'N', NULL, NULL, NULL, 'string', 'N', 'test.accounts');

INSERT INTO KFK_PARAM_TEMPLATE (id, param_name, input_method, param_explain, param_key, par_class_id, dp_level, dp_order, hidden_yn, group_cd, par_param_id, par_column_key, column_type, required_yn, default_value) 
VALUES ('PT-L2-002', 'Mode', 'TEXT', 'Query mode (bulk, incrementing, timestamp, timestamp+incrementing)', 'mode', 'io.confluent.connect.jdbc.JdbcSourceConnector', 2, 20, 'N', NULL, NULL, NULL, 'string', 'Y', 'bulk');

-- ------------------------------------------------------------------------------
-- JDBC Source Connector Specific Parameters (Level 3)
-- ------------------------------------------------------------------------------
INSERT INTO KFK_PARAM_TEMPLATE (id, param_name, input_method, param_explain, param_key, par_class_id, dp_level, dp_order, hidden_yn, group_cd, par_param_id, par_column_key, column_type, required_yn, default_value) 
VALUES ('PT-L3-001', 'Tasks Max', 'TEXT', 'Max connector tasks', 'tasks.max', 'io.confluent.connect.jdbc.JdbcSourceConnector', 3, 10, 'N', NULL, NULL, NULL, 'string', 'Y', '1');

-- ------------------------------------------------------------------------------
-- JDBC Sink Connector Specific Parameters (Level 1)
-- ------------------------------------------------------------------------------
INSERT INTO KFK_PARAM_TEMPLATE (id, param_name, input_method, param_explain, param_key, par_class_id, dp_level, dp_order, hidden_yn, group_cd, par_param_id, par_column_key, column_type, required_yn, default_value) 
VALUES ('SK-L1-001', 'Topics', 'TEXT', 'Comma-separated list of topics to consume from', 'topics', 'io.confluent.connect.jdbc.JdbcSinkConnector', 1, 10, 'N', NULL, NULL, NULL, 'string', 'Y', 'tp-dtlcd');

INSERT INTO KFK_PARAM_TEMPLATE (id, param_name, input_method, param_explain, param_key, par_class_id, dp_level, dp_order, hidden_yn, group_cd, par_param_id, par_column_key, column_type, required_yn, default_value) 
VALUES ('SK-L1-002', 'Key Converter', 'TEXT', 'Converter for message keys', 'key.converter', 'io.confluent.connect.jdbc.JdbcSinkConnector', 1, 20, 'N', NULL, NULL, NULL, 'string', 'Y', 'io.confluent.connect.avro.AvroConverter');

INSERT INTO KFK_PARAM_TEMPLATE (id, param_name, input_method, param_explain, param_key, par_class_id, dp_level, dp_order, hidden_yn, group_cd, par_param_id, par_column_key, column_type, required_yn, default_value) 
VALUES ('SK-L1-004', 'Value Converter', 'TEXT', 'Converter for message values', 'value.converter', 'io.confluent.connect.jdbc.JdbcSinkConnector', 1, 30, 'N', NULL, NULL, NULL, 'string', 'Y', 'io.confluent.connect.avro.AvroConverter');

INSERT INTO KFK_PARAM_TEMPLATE (id, param_name, input_method, param_explain, param_key, par_class_id, dp_level, dp_order, hidden_yn, group_cd, par_param_id, par_column_key, column_type, required_yn, default_value) 
VALUES ('SK-L1-007', 'Connection URL', 'AUTO', 'JDBC URL for Sink DB', 'connection.url', 'io.confluent.connect.jdbc.JdbcSinkConnector', 1, 40, 'Y', NULL, NULL, NULL, 'string', 'Y', '');

INSERT INTO KFK_PARAM_TEMPLATE (id, param_name, input_method, param_explain, param_key, par_class_id, dp_level, dp_order, hidden_yn, group_cd, par_param_id, par_column_key, column_type, required_yn, default_value) 
VALUES ('SK-L1-008', 'Connection User', 'AUTO', 'DB Username', 'connection.username', 'io.confluent.connect.jdbc.JdbcSinkConnector', 1, 50, 'Y', NULL, NULL, NULL, 'string', 'Y', '');

INSERT INTO KFK_PARAM_TEMPLATE (id, param_name, input_method, param_explain, param_key, par_class_id, dp_level, dp_order, hidden_yn, group_cd, par_param_id, par_column_key, column_type, required_yn, default_value) 
VALUES ('SK-L1-009', 'Connection Password', 'AUTO', 'DB Password', 'connection.password', 'io.confluent.connect.jdbc.JdbcSinkConnector', 1, 60, 'Y', NULL, NULL, NULL, 'string', 'Y', '');

-- ------------------------------------------------------------------------------
-- JDBC Sink Connector Specific Parameters (Level 2)
-- ------------------------------------------------------------------------------
INSERT INTO KFK_PARAM_TEMPLATE (id, param_name, input_method, param_explain, param_key, par_class_id, dp_level, dp_order, hidden_yn, group_cd, par_param_id, par_column_key, column_type, required_yn, default_value) 
VALUES ('SK-L2-001', 'Insert Mode', 'TEXT', 'Insert mode (insert, upsert, update)', 'insert.mode', 'io.confluent.connect.jdbc.JdbcSinkConnector', 2, 10, 'N', NULL, NULL, NULL, 'string', 'Y', 'insert');

INSERT INTO KFK_PARAM_TEMPLATE (id, param_name, input_method, param_explain, param_key, par_class_id, dp_level, dp_order, hidden_yn, group_cd, par_param_id, par_column_key, column_type, required_yn, default_value) 
VALUES ('SK-L2-002', 'Table Name Format', 'TEXT', 'Format string for destination table name', 'table.name.format', 'io.confluent.connect.jdbc.JdbcSinkConnector', 2, 20, 'N', NULL, NULL, NULL, 'string', 'Y', '${topic}');

-- ------------------------------------------------------------------------------
-- JDBC Sink Connector Specific Parameters (Level 3)
-- ------------------------------------------------------------------------------
INSERT INTO KFK_PARAM_TEMPLATE (id, param_name, input_method, param_explain, param_key, par_class_id, dp_level, dp_order, hidden_yn, group_cd, par_param_id, par_column_key, column_type, required_yn, default_value) 
VALUES ('SK-L3-001', 'Tasks Max', 'TEXT', 'Max connector tasks', 'tasks.max', 'io.confluent.connect.jdbc.JdbcSinkConnector', 3, 10, 'N', NULL, NULL, NULL, 'string', 'Y', '1');
