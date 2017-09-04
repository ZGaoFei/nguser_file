package cn.com.nggirl.nguser.utils.downloadutils;


import android.net.Uri;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import cn.com.nggirl.commons.FileOperationUtils;
import cn.com.nggirl.nguser.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class DownloadFileWithProgressModel {
    // 默认文件保存路径
    private String filePath = FileOperationUtils.getExternalCacheDirectory();

    private DownloadUrlApi api;
    private static DownloadFileWithProgressModel model;

    private Retrofit retrofit;
    private DownloadView view;

    public static DownloadFileWithProgressModel instance(DownloadView view) {
        if (model == null) {
            synchronized (DownloadFileWithProgressModel.class) {
                if (model == null) {
                    model = new DownloadFileWithProgressModel(view);
                }
            }
        }
        return model;
    }

    private DownloadFileWithProgressModel(final DownloadView view) {
        this.view = view;

        DownloadProgressInterceptor interceptor = new DownloadProgressInterceptor(new DownloadProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength, boolean done) {
                view.update((int) (bytesRead * 100 / contentLength));
            }
        });

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .retryOnConnectionFailure(true)
                .connectTimeout(10_000, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER)
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();


        api = retrofit.create(DownloadUrlApi.class);
    }

    public void downloadWithProgress(@Nullable final String url) {
        api.downloadFileFromNet(url).subscribeOn(Schedulers.io())
                .subscribeOn(Schedulers.computation())
                .map(new Func1<ResponseBody, File>() {
                    @Override
                    public File call(ResponseBody responseBody) {
                        return writeResponseBodyToDisk(responseBody, getFileNameFromUrl(url));
                    }
                }).subscribe(new Subscriber<File>() {
            @Override
            public void onCompleted() {
                view.commplete();
            }

            @Override
            public void onError(Throwable e) {
                view.error(e);
            }

            @Override
            public void onNext(File file) {
                view.onNext(file);
            }
        });
    }

    /**
     * 将下载的内容根据文件名称保存在项目的文件路径下
     * <p>
     * 路径为：/storage/emulated/0/nggirl/cache/
     *
     * @param body
     * @param fileName
     * @return
     */
    private File writeResponseBodyToDisk(ResponseBody body, String fileName) {
        try {
            File futureStudioIconFile = new File(filePath + "//" + fileName);

            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
                }

                outputStream.flush();
                return futureStudioIconFile;
            } catch (IOException e) {
                return null;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 根据文件路径获取文件名称
     * <p>
     * 根据文件的路径来获取文件名称，包含有后缀
     * 图片文件会根据 @ 符号进行切分，以获取真实名称
     * 其他文件中不包含 @ 符号，会返回文件名
     *
     * @param url
     * @return
     */
    private String getFileNameFromUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return url;
        } else {
            Uri parse = Uri.parse(url);
            String path = parse.getPath();// 获取文件路径包含有文件名称
            if (!TextUtils.isEmpty(path)) {
                String substring = path.substring(path.lastIndexOf("/") + 1, path.length());// 获取的名称包含后面的图片限制信息
                if (substring.contains("@")) {
                    return substring.substring(0, substring.indexOf("@"));// 去除限制信息，获得名字，包含后缀
                } else {
                    return substring;
                }
            }
        }

        return null;
    }

    /**
     * 自定义文件路径，默认为项目的cache文件下
     * 需要在下载前进行设置
     *
     * @param filePath
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
