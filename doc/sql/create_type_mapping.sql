-- DDL 타입 매핑 테이블 생성
CREATE TABLE `zxxmig_type_mapping` (
  `mapping_seq` varchar(20) NOT NULL COMMENT 'PK',
  `src_db_type` varchar(20) NOT NULL COMMENT '소스 DB 유형 (oracle, mysql, postgresql 등)',
  `src_type_name` varchar(50) NOT NULL COMMENT '소스 데이터 타입명 (대문자)',
  `tgt_db_type` varchar(20) NOT NULL COMMENT '타겟 DB 유형 (oracle, mysql, postgresql 등)',
  `tgt_type_name` varchar(50) NOT NULL COMMENT '변환될 타겟 데이터 타입명',
  `priority` int DEFAULT 10 COMMENT '매핑 우선순위 (낮을수록 먼저 적용)',
  `use_yn` varchar(1) DEFAULT 'Y' COMMENT '사용 여부',
  `create_date` datetime DEFAULT NULL COMMENT '등록일',
  `update_date` datetime DEFAULT NULL COMMENT '수정일',
  PRIMARY KEY (`mapping_seq`),
  KEY `idx_mapping_lookup` (`src_db_type`, `src_type_name`, `tgt_db_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='DDL 데이터 타입 변환 매핑 정보';

-- 기본 Oracle -> PostgreSQL 매핑 데이터 삽입
INSERT INTO `zxxmig_type_mapping` (mapping_seq, src_db_type, src_type_name, tgt_db_type, tgt_type_name, priority, use_yn, create_date, update_date) VALUES
('TM-001', 'oracle', 'VARCHAR2', 'postgresql', 'VARCHAR', 10, 'Y', NOW(), NOW()),
('TM-002', 'oracle', 'NVARCHAR2', 'postgresql', 'VARCHAR', 10, 'Y', NOW(), NOW()),
('TM-003', 'oracle', 'NUMBER', 'postgresql', 'NUMERIC', 20, 'Y', NOW(), NOW()),
('TM-004', 'oracle', 'CLOB', 'postgresql', 'TEXT', 10, 'Y', NOW(), NOW()),
('TM-005', 'oracle', 'BLOB', 'postgresql', 'BYTEA', 10, 'Y', NOW(), NOW()),
('TM-006', 'oracle', 'DATE', 'postgresql', 'TIMESTAMP', 10, 'Y', NOW(), NOW()),
('TM-007', 'oracle', 'LONG', 'postgresql', 'TEXT', 10, 'Y', NOW(), NOW());

-- 기본 Oracle -> MariaDB/MySQL 매핑 데이터 삽입
INSERT INTO `zxxmig_type_mapping` (mapping_seq, src_db_type, src_type_name, tgt_db_type, tgt_type_name, priority, use_yn, create_date, update_date) VALUES
('TM-101', 'oracle', 'VARCHAR2', 'maria', 'VARCHAR', 10, 'Y', NOW(), NOW()),
('TM-102', 'oracle', 'NVARCHAR2', 'maria', 'VARCHAR', 10, 'Y', NOW(), NOW()),
('TM-103', 'oracle', 'NUMBER', 'maria', 'DECIMAL', 20, 'Y', NOW(), NOW()),
('TM-104', 'oracle', 'CLOB', 'maria', 'LONGTEXT', 10, 'Y', NOW(), NOW()),
('TM-105', 'oracle', 'BLOB', 'maria', 'LONGBLOB', 10, 'Y', NOW(), NOW()),
('TM-106', 'oracle', 'DATE', 'maria', 'DATETIME', 10, 'Y', NOW(), NOW());

-- 기본 Oracle -> MSSQL 매핑 데이터 삽입
INSERT INTO `zxxmig_type_mapping` (mapping_seq, src_db_type, src_type_name, tgt_db_type, tgt_type_name, priority, use_yn, create_date, update_date) VALUES
('TM-201', 'oracle', 'VARCHAR2', 'mssql', 'VARCHAR', 10, 'Y', NOW(), NOW()),
('TM-202', 'oracle', 'NVARCHAR2', 'mssql', 'NVARCHAR', 10, 'Y', NOW(), NOW()),
('TM-203', 'oracle', 'NUMBER', 'mssql', 'DECIMAL', 20, 'Y', NOW(), NOW()),
('TM-204', 'oracle', 'CLOB', 'mssql', 'VARCHAR(MAX)', 10, 'Y', NOW(), NOW()),
('TM-205', 'oracle', 'BLOB', 'mssql', 'VARBINARY(MAX)', 10, 'Y', NOW(), NOW()),
('TM-206', 'oracle', 'DATE', 'mssql', 'DATETIME2', 10, 'Y', NOW(), NOW()),
('TM-207', 'oracle', 'TIMESTAMP', 'mssql', 'DATETIME2', 10, 'Y', NOW(), NOW());

-- MSSQL -> Oracle 매핑
INSERT INTO `zxxmig_type_mapping` (mapping_seq, src_db_type, src_type_name, tgt_db_type, tgt_type_name, priority, use_yn, create_date, update_date) VALUES
('TM-301', 'mssql', 'VARCHAR', 'oracle', 'VARCHAR2', 10, 'Y', NOW(), NOW()),
('TM-302', 'mssql', 'NVARCHAR', 'oracle', 'NVARCHAR2', 10, 'Y', NOW(), NOW()),
('TM-303', 'mssql', 'INT', 'oracle', 'NUMBER', 10, 'Y', NOW(), NOW()),
('TM-304', 'mssql', 'BIGINT', 'oracle', 'NUMBER', 10, 'Y', NOW(), NOW()),
('TM-305', 'mssql', 'DATETIME', 'oracle', 'DATE', 10, 'Y', NOW(), NOW()),
('TM-306', 'mssql', 'DATETIME2', 'oracle', 'TIMESTAMP', 10, 'Y', NOW(), NOW()),
('TM-307', 'mssql', 'VARCHAR(MAX)', 'oracle', 'CLOB', 10, 'Y', NOW(), NOW()),
('TM-308', 'mssql', 'VARBINARY(MAX)', 'oracle', 'BLOB', 10, 'Y', NOW(), NOW());

-- MSSQL -> MariaDB/MySQL 매핑
INSERT INTO `zxxmig_type_mapping` (mapping_seq, src_db_type, src_type_name, tgt_db_type, tgt_type_name, priority, use_yn, create_date, update_date) VALUES
('TM-401', 'mssql', 'VARCHAR', 'maria', 'VARCHAR', 10, 'Y', NOW(), NOW()),
('TM-402', 'mssql', 'NVARCHAR', 'maria', 'VARCHAR', 10, 'Y', NOW(), NOW()),
('TM-403', 'mssql', 'DATETIME', 'maria', 'DATETIME', 10, 'Y', NOW(), NOW()),
('TM-404', 'mssql', 'DATETIME2', 'maria', 'DATETIME', 10, 'Y', NOW(), NOW()),
('TM-405', 'mssql', 'VARCHAR(MAX)', 'maria', 'LONGTEXT', 10, 'Y', NOW(), NOW()),
('TM-406', 'mssql', 'VARBINARY(MAX)', 'maria', 'LONGBLOB', 10, 'Y', NOW(), NOW());

-- MSSQL -> PostgreSQL 매핑
INSERT INTO `zxxmig_type_mapping` (mapping_seq, src_db_type, src_type_name, tgt_db_type, tgt_type_name, priority, use_yn, create_date, update_date) VALUES
('TM-501', 'mssql', 'VARCHAR', 'postgresql', 'VARCHAR', 10, 'Y', NOW(), NOW()),
('TM-502', 'mssql', 'NVARCHAR', 'postgresql', 'VARCHAR', 10, 'Y', NOW(), NOW()),
('TM-503', 'mssql', 'DATETIME', 'postgresql', 'TIMESTAMP', 10, 'Y', NOW(), NOW()),
('TM-504', 'mssql', 'DATETIME2', 'postgresql', 'TIMESTAMP', 10, 'Y', NOW(), NOW()),
('TM-505', 'mssql', 'VARCHAR(MAX)', 'postgresql', 'TEXT', 10, 'Y', NOW(), NOW()),
('TM-506', 'mssql', 'VARBINARY(MAX)', 'postgresql', 'BYTEA', 10, 'Y', NOW(), NOW());

-- PostgreSQL -> MariaDB 매핑
INSERT INTO `zxxmig_type_mapping` (mapping_seq, src_db_type, src_type_name, tgt_db_type, tgt_type_name, priority, use_yn, create_date, update_date) VALUES
('TM-601', 'postgresql', 'VARCHAR', 'maria', 'VARCHAR', 10, 'Y', NOW(), NOW()),
('TM-602', 'postgresql', 'TEXT', 'maria', 'LONGTEXT', 10, 'Y', NOW(), NOW()),
('TM-603', 'postgresql', 'NUMERIC', 'maria', 'DECIMAL', 10, 'Y', NOW(), NOW()),
('TM-604', 'postgresql', 'TIMESTAMP', 'maria', 'DATETIME', 10, 'Y', NOW(), NOW()),
('TM-605', 'postgresql', 'BYTEA', 'maria', 'LONGBLOB', 10, 'Y', NOW(), NOW()),
('TM-606', 'postgresql', 'UUID', 'maria', 'VARCHAR(36)', 10, 'Y', NOW(), NOW());

-- PostgreSQL -> Oracle 매핑
INSERT INTO `zxxmig_type_mapping` (mapping_seq, src_db_type, src_type_name, tgt_db_type, tgt_type_name, priority, use_yn, create_date, update_date) VALUES
('TM-701', 'postgresql', 'VARCHAR', 'oracle', 'VARCHAR2', 10, 'Y', NOW(), NOW()),
('TM-702', 'postgresql', 'TEXT', 'oracle', 'CLOB', 10, 'Y', NOW(), NOW()),
('TM-703', 'postgresql', 'NUMERIC', 'oracle', 'NUMBER', 10, 'Y', NOW(), NOW()),
('TM-704', 'postgresql', 'TIMESTAMP', 'oracle', 'DATE', 10, 'Y', NOW(), NOW()),
('TM-705', 'postgresql', 'BYTEA', 'oracle', 'BLOB', 10, 'Y', NOW(), NOW());

-- MariaDB -> PostgreSQL 매핑
INSERT INTO `zxxmig_type_mapping` (mapping_seq, src_db_type, src_type_name, tgt_db_type, tgt_type_name, priority, use_yn, create_date, update_date) VALUES
('TM-801', 'maria', 'VARCHAR', 'postgresql', 'VARCHAR', 10, 'Y', NOW(), NOW()),
('TM-802', 'maria', 'LONGTEXT', 'postgresql', 'TEXT', 10, 'Y', NOW(), NOW()),
('TM-803', 'maria', 'DECIMAL', 'postgresql', 'NUMERIC', 10, 'Y', NOW(), NOW()),
('TM-804', 'maria', 'DATETIME', 'postgresql', 'TIMESTAMP', 10, 'Y', NOW(), NOW()),
('TM-805', 'maria', 'LONGBLOB', 'postgresql', 'BYTEA', 10, 'Y', NOW(), NOW());

-- MariaDB -> Oracle 매핑
INSERT INTO `zxxmig_type_mapping` (mapping_seq, src_db_type, src_type_name, tgt_db_type, tgt_type_name, priority, use_yn, create_date, update_date) VALUES
('TM-901', 'maria', 'VARCHAR', 'oracle', 'VARCHAR2', 10, 'Y', NOW(), NOW()),
('TM-902', 'maria', 'LONGTEXT', 'oracle', 'CLOB', 10, 'Y', NOW(), NOW()),
('TM-903', 'maria', 'DECIMAL', 'oracle', 'NUMBER', 10, 'Y', NOW(), NOW()),
('TM-904', 'maria', 'DATETIME', 'oracle', 'DATE', 10, 'Y', NOW(), NOW()),
('TM-905', 'maria', 'LONGBLOB', 'oracle', 'BLOB', 10, 'Y', NOW(), NOW());
