package com.example.administrator.test10;

public interface DownloadListener {
    void onProgress(int progress);
    void onSuccess();
    void onError();
    void onPaused();
    void onCanceled();
}
