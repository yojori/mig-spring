-- Configuration for 53M Row Migration Test (srm10dp.esaatth)
-- Strategy: THREAD_IDX (using Native UUID Partitioning with BigInteger Logic)

-- 1. Master Entry
DELETE FROM ZXXMIG_MIGRATION_MASTER WHERE master_code = 'mig_test_53m';
INSERT INTO ZXXMIG_MIGRATION_MASTER (master_code, master_name, source_db_code, target_db_code)
VALUES ('mig_test_53m', '53M Data Migration', 'SOURCE', 'TARGET');

-- 2. Migration List 
-- Reverting to simple SELECT * FROM ...
-- Using 'att_id' as PK because sys_id is constant.
-- The Java Worker now handles UUID range logic automatically!
DELETE FROM ZXXMIG_MIGRATION_LIST WHERE mig_master = 'mig_test_53m';
INSERT INTO ZXXMIG_MIGRATION_LIST (
    mig_list_seq, mig_master, mig_name, 
    mig_type, thread_use_yn, thread_count, page_count_per_thread, 
    sql_string, 
    ordering, execute_yn, display_yn
) VALUES (
    '53M_LIST_001', 'mig_test_53m', 'SRM Attachment Migration', 
    'THREAD_IDX', 'Y', 4, 1000, 
    'SELECT * FROM srm10dp.esaatth', 
    1, 'Y', 'Y'
);

-- CLEANUP
DELETE FROM ZXXMIG_INSERT_TABLE WHERE mig_list_seq = '53M_LIST_001';

-- 3. Insert SQL Configuration
DELETE FROM ZXXMIG_INSERT_SQL WHERE mig_list_seq = '53M_LIST_001';
INSERT INTO ZXXMIG_INSERT_SQL (
    insert_sql_seq, mig_list_seq, insert_type, insert_table, pk_column, truncate_yn
) VALUES (
    '53M_SQL_001', '53M_LIST_001', 'INSERT', 'srm10dp.esaatth', 'att_id', 'Y'
);

-- 4. Column Mapping
-- (Simple mapping since we select *)
DELETE FROM ZXXMIG_INSERT_COLUMN WHERE insert_sql_seq = '53M_SQL_001';
INSERT INTO ZXXMIG_INSERT_COLUMN (insert_column_seq, insert_sql_seq, column_name, insert_data, use_yn) VALUES ('53M_COL_01', '53M_SQL_001', 'sys_id', 'sys_id', 'Y');
INSERT INTO ZXXMIG_INSERT_COLUMN (insert_column_seq, insert_sql_seq, column_name, insert_data, use_yn) VALUES ('53M_COL_02', '53M_SQL_001', 'att_id', 'att_id', 'Y');
INSERT INTO ZXXMIG_INSERT_COLUMN (insert_column_seq, insert_sql_seq, column_name, insert_data, use_yn) VALUES ('53M_COL_03', '53M_SQL_001', 'grp_cd', 'grp_cd', 'Y');
INSERT INTO ZXXMIG_INSERT_COLUMN (insert_column_seq, insert_sql_seq, column_name, insert_data, use_yn) VALUES ('53M_COL_04', '53M_SQL_001', 'orgn_file_nm', 'orgn_file_nm', 'Y');
INSERT INTO ZXXMIG_INSERT_COLUMN (insert_column_seq, insert_sql_seq, column_name, insert_data, use_yn) VALUES ('53M_COL_05', '53M_SQL_001', 'att_file_nm', 'att_file_nm', 'Y');
INSERT INTO ZXXMIG_INSERT_COLUMN (insert_column_seq, insert_sql_seq, column_name, insert_data, use_yn) VALUES ('53M_COL_06', '53M_SQL_001', 'att_file_path', 'att_file_path', 'Y');
INSERT INTO ZXXMIG_INSERT_COLUMN (insert_column_seq, insert_sql_seq, column_name, insert_data, use_yn) VALUES ('53M_COL_07', '53M_SQL_001', 'att_file_siz', 'att_file_siz', 'Y');
INSERT INTO ZXXMIG_INSERT_COLUMN (insert_column_seq, insert_sql_seq, column_name, insert_data, use_yn) VALUES ('53M_COL_08', '53M_SQL_001', 'sort_ord', 'sort_ord', 'Y');
INSERT INTO ZXXMIG_INSERT_COLUMN (insert_column_seq, insert_sql_seq, column_name, insert_data, use_yn) VALUES ('53M_COL_09', '53M_SQL_001', 'rem', 'rem', 'Y');
INSERT INTO ZXXMIG_INSERT_COLUMN (insert_column_seq, insert_sql_seq, column_name, insert_data, use_yn) VALUES ('53M_COL_10', '53M_SQL_001', 'sts', 'sts', 'Y');
INSERT INTO ZXXMIG_INSERT_COLUMN (insert_column_seq, insert_sql_seq, column_name, insert_data, use_yn) VALUES ('53M_COL_11', '53M_SQL_001', 'reg_id', 'reg_id', 'Y');
INSERT INTO ZXXMIG_INSERT_COLUMN (insert_column_seq, insert_sql_seq, column_name, insert_data, use_yn) VALUES ('53M_COL_12', '53M_SQL_001', 'reg_dt', 'reg_dt', 'Y');
INSERT INTO ZXXMIG_INSERT_COLUMN (insert_column_seq, insert_sql_seq, column_name, insert_data, use_yn) VALUES ('53M_COL_13', '53M_SQL_001', 'mod_id', 'mod_id', 'Y');
INSERT INTO ZXXMIG_INSERT_COLUMN (insert_column_seq, insert_sql_seq, column_name, insert_data, use_yn) VALUES ('53M_COL_14', '53M_SQL_001', 'mod_dt', 'mod_dt', 'Y');

-- 5. Create Work Task
INSERT INTO ZXXMIG_WORK_LIST (
  work_seq, mig_list_seq, status, 
  thread_count, page_count_per_thread, ordered, start_date
) VALUES (
  'WORK_53M_005', '53M_LIST_001', 'READY', 
  4, 1000, 1, NOW()
);
