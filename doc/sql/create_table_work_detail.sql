-- migration.zxxmig_work_detail definition

CREATE TABLE `zxxmig_work_detail` (
  `detail_seq` int(11) NOT NULL AUTO_INCREMENT COMMENT '상세 로그 PK',
  `work_seq` int(11) NOT NULL COMMENT '작업 FK (zxxmig_work_list)',
  `thread_idx` int(11) DEFAULT '0' COMMENT '쓰레드 인덱스',
  `paging_idx` int(11) DEFAULT '0' COMMENT '페이징 인덱스',
  `query_params` varchar(4000) COMMENT '실행 파라미터 (PK 범위 등)',
  `read_cnt` int(11) DEFAULT '0' COMMENT '읽은 건수',
  `read_ms` int(11) DEFAULT '0' COMMENT '읽기 소요 시간(ms)',
  `proc_cnt` int(11) DEFAULT '0' COMMENT '처리 건수',
  `proc_ms` int(11) DEFAULT '0' COMMENT '처리 소요 시간(ms)',
  `status` varchar(20) DEFAULT 'SUCCESS' COMMENT '상태 (SUCCESS, FAIL)',
  `err_msg` varchar(4000) COMMENT '에러 메시지',
  `create_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
  PRIMARY KEY (`detail_seq`),
  KEY `IDX_WORK_DETAIL_WORK_SEQ` (`work_seq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='이관 작업 상세 로그 (쓰레드/배치 단위)';
