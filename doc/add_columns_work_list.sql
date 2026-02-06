-- Add columns for progress tracking
ALTER TABLE zxxmig_work_list ADD read_count BIGINT DEFAULT 0;
ALTER TABLE zxxmig_work_list ADD proc_count BIGINT DEFAULT 0;
