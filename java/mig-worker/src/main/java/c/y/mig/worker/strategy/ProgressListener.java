package c.y.mig.worker.strategy;

public interface ProgressListener {
    void onProgress(long readCount, long procCount);
}
