package com.yojori.migration.worker.strategy;

public interface ProgressListener {
    void onProgress(long readCount, long procCount);
}
