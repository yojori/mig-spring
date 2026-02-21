-- migration.zxxmig_work_list definition

CREATE TABLE `zxxmig_work_list` (
  `work_seq` varchar(20) NOT NULL COMMENT '작업 PK',
  `mig_list_seq` varchar(20) NOT NULL COMMENT '이관 목록 FK',
  `worker_id` varchar(50) DEFAULT NULL COMMENT '작업을 수행한 워커 ID',
  `status` varchar(20) DEFAULT 'READY' COMMENT '상태 (READY, RUNNING, DONE, FAIL)',
  `start_date` datetime DEFAULT NULL COMMENT '작업 시작 시간',
  `end_date` datetime DEFAULT NULL COMMENT '작업 종료 시간',
  `result_msg` text COMMENT '결과 메시지 (성공 요약 또는 에러 로그)',
  `create_date` datetime DEFAULT NULL COMMENT '작업 생성일',
  PRIMARY KEY (`work_seq`),
  KEY `FK_WORK_LIST_MIG_LIST_idx` (`mig_list_seq`),
  CONSTRAINT `FK_WORK_LIST_MIG_LIST` FOREIGN KEY (`mig_list_seq`) REFERENCES `zxxmig_migration_list` (`mig_list_seq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='이관 작업 목록 (실행 이력)';
