-- Force update the PK column to 'mig_pk' for the 53M test
-- The error indicates it is currently set to 'att_id'

UPDATE ZXXMIG_INSERT_SQL 
SET pk_column = 'mig_pk' 
WHERE mig_list_seq = '53M_LIST_001';

COMMIT;

-- Verify the change (Optional)
-- SELECT pk_column FROM ZXXMIG_INSERT_SQL WHERE mig_list_seq = '53M_LIST_001';
