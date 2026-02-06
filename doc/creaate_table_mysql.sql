-- migration.zxxmig_db_master definition

CREATE TABLE `zxxmig_db_master` (
  `master_code` varchar(20) NOT NULL COMMENT 'PK',
  `url` varchar(45) DEFAULT NULL COMMENT '접근 URL (공통에서 DB 버젼 등을 선택하면 값을 가져옴',
  `class_name` varchar(45) DEFAULT NULL COMMENT 'jdbc class name',
  `user_id` varchar(45) DEFAULT NULL COMMENT '사용자 ID',
  `user_pswd` varchar(45) DEFAULT NULL COMMENT '사용자 PSWD',
  `character_set` varchar(45) DEFAULT NULL COMMENT '캐릭터 셋',
  `create_date` datetime DEFAULT NULL COMMENT '등록일',
  `update_date` datetime DEFAULT NULL COMMENT '수정일',
  `db_type` varchar(20) DEFAULT NULL COMMENT 'DB 종료 (Oracle, Mysql, Mssql...)',
  PRIMARY KEY (`master_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='DB 연결 마스터';


-- migration.zxxmig_migration_master definition

CREATE TABLE `zxxmig_migration_master` (
  `master_code` varchar(20) NOT NULL COMMENT '이관 Master 코드 (PK)',
  `source_db_type` varchar(20) DEFAULT NULL COMMENT '소스 DB Type (Oracle, MSsql, Mysql 등 코드값)',
  `source_charset` varchar(20) DEFAULT NULL COMMENT '소스의 캐릭터셋',
  `target_charset` varchar(20) DEFAULT NULL COMMENT '타겟의 캐릭터셋',
  `target_db_type` varchar(20) DEFAULT NULL COMMENT '타겟 db 종류 (Oracle, MSsql, ...) 코드값',
  `source_db_alias` varchar(20) DEFAULT NULL COMMENT 'source pool alias',
  `target_db_alias` varchar(20) DEFAULT NULL COMMENT 'target pool alias',
  `create_date` datetime DEFAULT NULL COMMENT '등록일',
  `update_date` datetime DEFAULT NULL COMMENT '수정일',
  `master_name` varchar(200) DEFAULT NULL COMMENT '이관명',
  `display_yn` varchar(1) DEFAULT NULL,
  `ordering` int DEFAULT NULL,
  PRIMARY KEY (`master_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='이관 Master';


-- migration.zxxmig_migration_list definition

CREATE TABLE `zxxmig_migration_list` (
  `mig_list_seq` varchar(20) NOT NULL COMMENT '이관 목록 PK',
  `mig_master` varchar(20) DEFAULT NULL COMMENT '이관 Master (FK)',
  `thread_use_yn` varchar(1) DEFAULT NULL COMMENT 'Thread 사용 여부',
  `thread_count` int DEFAULT NULL COMMENT 'Thread 개수',
  `page_count_per_thread` int DEFAULT NULL COMMENT 'Thread 당 처리 개수',
  `sql_string` text COMMENT 'select sql',
  `ordering` int DEFAULT NULL COMMENT '정렬',
  `create_date` datetime DEFAULT NULL COMMENT '등록일',
  `update_date` datetime DEFAULT NULL COMMENT '수정일',
  `execute_yn` varchar(1) DEFAULT NULL COMMENT '실행 여부 (일괄실해에서 제외)',
  `mig_name` varchar(200) DEFAULT NULL COMMENT '이관명',
  `mig_type` varchar(20) DEFAULT NULL COMMENT '이관 유형\n(TABLE TO TABLE), 단순 select insert,,,,등 여러가지를 생각해서 유형을 만들어야 함',
  `source_db_alias` varchar(20) DEFAULT NULL COMMENT '소스 DB',
  `target_db_alias` varchar(20) DEFAULT NULL COMMENT 'Target DB',
  `display_yn` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`mig_list_seq`),
  KEY `FK_MIG_LIST_MIG_MASTER_idx` (`mig_master`),
  CONSTRAINT `FK_MIG_LIST_MIG_MASTER` FOREIGN KEY (`mig_master`) REFERENCES `zxxmig_migration_master` (`master_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- migration.zxxmig_select_column definition

CREATE TABLE `zxxmig_select_column` (
  `column_seq` varchar(20) NOT NULL COMMENT 'PK',
  `mig_list_seq` varchar(20) DEFAULT NULL COMMENT '이관 목록 (FK)',
  `column_name` varchar(20) DEFAULT NULL COMMENT '컬럼명',
  `column_type` varchar(50) DEFAULT NULL COMMENT '컬럼 Type',
  `create_date` datetime DEFAULT NULL COMMENT '등록일',
  `update_date` datetime DEFAULT NULL COMMENT '수정일',
  `ordering` int DEFAULT NULL COMMENT '정렬',
  PRIMARY KEY (`column_seq`),
  KEY `FK_MIG_LIST_idx` (`mig_list_seq`),
  CONSTRAINT `FK_MIG_LIST` FOREIGN KEY (`mig_list_seq`) REFERENCES `zxxmig_migration_list` (`mig_list_seq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='이관 select column';


-- migration.zxxmig_insert_sql definition

CREATE TABLE `zxxmig_insert_sql` (
  `insert_sql_seq` varchar(20) NOT NULL COMMENT '이관 Target sql PK',
  `mig_list_seq` varchar(20) DEFAULT NULL COMMENT '이관 목록 (FK)',
  `insert_type` varchar(20) DEFAULT NULL COMMENT '이관 Type (insert, update, delete)',
  `insert_table` varchar(200) DEFAULT NULL COMMENT 'sql',
  `create_date` datetime DEFAULT NULL COMMENT '등록일',
  `update_date` datetime DEFAULT NULL COMMENT '수정일',
  `pk_column` varchar(200) DEFAULT NULL,
  `truncate_yn` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`insert_sql_seq`),
  KEY `FK_MIG_LIST_INSERT_idx` (`mig_list_seq`),
  CONSTRAINT `FK_MIG_LIST_INSERT` FOREIGN KEY (`mig_list_seq`) REFERENCES `zxxmig_migration_list` (`mig_list_seq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='이관 Target sql';


-- migration.zxxmig_insert_table definition

CREATE TABLE `zxxmig_insert_table` (
  `insert_table_seq` varchar(20) NOT NULL COMMENT 'PK',
  `source_table` varchar(45) DEFAULT NULL COMMENT 'source 테이블',
  `source_pk` varchar(45) DEFAULT NULL COMMENT 'source 테이블 PK 컬럼명',
  `target_table` varchar(45) DEFAULT NULL COMMENT 'target 테이블',
  `create_date` datetime DEFAULT NULL COMMENT '등록일',
  `update_date` datetime DEFAULT NULL COMMENT '수정일',
  `mig_list_seq` varchar(20) DEFAULT NULL COMMENT 'migration List seq (FK)',
  `truncate_yn` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`insert_table_seq`),
  KEY `FK_insert_table_idx` (`mig_list_seq`),
  CONSTRAINT `FK_insert_table` FOREIGN KEY (`mig_list_seq`) REFERENCES `zxxmig_migration_list` (`mig_list_seq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='테이블 단위 Insert 목록';


-- migration.zxxmig_insert_column definition

CREATE TABLE `zxxmig_insert_column` (
  `insert_column_seq` varchar(20) NOT NULL COMMENT 'PK',
  `insert_sql_seq` varchar(20) DEFAULT NULL COMMENT 'Insert_sql (FK)',
  `column_name` varchar(20) DEFAULT NULL COMMENT '컬럼명',
  `column_type` varchar(20) DEFAULT NULL COMMENT '컬럼 Type',
  `create_date` datetime DEFAULT NULL COMMENT '등록일',
  `update_date` datetime DEFAULT NULL COMMENT '수정일',
  `insert_data` varchar(200) DEFAULT NULL COMMENT 'selecf 절과 연결될 컬럼명',
  `insert_value` varchar(200) DEFAULT NULL COMMENT '직접입력한 insert value',
  `use_yn` varchar(1) DEFAULT NULL COMMENT '사용여부 Insert, Update sql 생성 시 사용여부',
  PRIMARY KEY (`insert_column_seq`),
  KEY `FK_INSERT_SQL_idx` (`insert_sql_seq`),
  CONSTRAINT `FK_INSERT_SQL` FOREIGN KEY (`insert_sql_seq`) REFERENCES `zxxmig_insert_sql` (`insert_sql_seq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='insert sql 의 컬럼';


-- migration.zxxmig_result_hd definition

CREATE TABLE `zxxmig_result_hd` (
  `RESULT_HD_SEQ` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'PK',
  `MIG_LIST_SEQ` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT 'MIG_LIST_SEQ FK',
  `MIG_NAME` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '이관명',
  `SELECT_COUNT` int DEFAULT NULL COMMENT '조회수',
  `INSERT_COUNT` int DEFAULT NULL COMMENT '입력수',
  `ERROR_COUNT` int DEFAULT NULL COMMENT '에러수',
  `CREATE_DATE` datetime DEFAULT NULL COMMENT '생성일',
  `UPDATE_DATE` datetime DEFAULT NULL COMMENT '수정일',
  PRIMARY KEY (`RESULT_HD_SEQ`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='이관 결과';


