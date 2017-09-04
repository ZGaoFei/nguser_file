package cn.com.nggirl.nguser.utils.downloadutils;


import java.io.File;

public interface DownloadView {

    void update(int progress);

    void error(Throwable e);

    void commplete();

    void onNext(File file);
}
