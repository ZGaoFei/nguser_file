package cn.com.nggirl.nguser.utils.downloadutils;


import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class DownloadProgressInterceptor implements Interceptor {

    private DownloadProgressListener listener;

    public DownloadProgressInterceptor(DownloadProgressListener listener) {
        this.listener = listener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response proceed = chain.proceed(chain.request());

        return proceed.newBuilder()
                .body(new DownloadResponseBody(proceed.body(), listener))
                .build();
    }

}
