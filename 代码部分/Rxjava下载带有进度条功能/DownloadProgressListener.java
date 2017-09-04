package cn.com.nggirl.nguser.utils.downloadutils;


public interface DownloadProgressListener {
    void update(long bytesRead, long contentLength, boolean done);
}
