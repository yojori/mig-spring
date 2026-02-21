-- OPTIMIZATION: Create Functional Index for fast Thread Partitioning
-- The migration uses "WHERE hashtext(att_id) BETWEEN ? AND ?"
-- Without this index, every thread performs a Full Table Scan (Slow!)

-- Drop old index if exists (for sys_id)
DROP INDEX IF EXISTS idx_esaatth_mig_pk;

-- Create new index for att_id
CREATE INDEX CONCURRENTLY idx_esaatth_mig_pk ON srm10dp.esaatth (hashtext(att_id));

-- After creating this index, the "SELECT ... WHERE mig_pk BETWEEN ..." queries 
-- will use an Index Range Scan, making them instant.
