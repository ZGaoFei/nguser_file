package cn.com.nggirl.nguser.mall.business.model;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.com.nggirl.commons.FileOperationUtils;
import cn.com.nggirl.nguser.core.RetrofitHelper;
import cn.com.nggirl.nguser.mall.business.protocol.ImageUrlApi;
import okhttp3.ResponseBody;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ImageUrlModel {
    private Context context;

    private ImageUrlApi api;
    private static ImageUrlModel model;

    public static ImageUrlModel getInstance(Context context) {
        if (model == null) {
            synchronized (ProductDetailsModel.class) {
                if (model == null) {
                    model = new ImageUrlModel(context);
                }
            }
        }
        return model;
    }

    public ImageUrlModel(Context context) {
        this.context = context;
        api = RetrofitHelper.createApi(ImageUrlApi.class);
    }

    public void downloadPicFromNet(final String fileUrl) {
        api.downloadPicFromNet(fileUrl)
                .subscribeOn(Schedulers.newThread())//在新线程中实现该方法
                .map(new Func1<ResponseBody, Bitmap>() {

                    @Override
                    public Bitmap call(ResponseBody arg0) {
                        if (writeResponseBodyToDisk(arg0, fileUrl)) {//保存图片成功
//                            Bitmap bitmap = BitmapFactory.decodeFile(context.getExternalFilesDir(null) + File.separator + "image.png");
//                            return bitmap;//返回一个bitmap对象
                        }
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//在Android主线程中展示
                .subscribe(new Subscriber<Bitmap>() {

                    @Override
                    public void onStart() {
                        super.onStart();
                    }

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable arg0) {
                    }

                    @Override
                    public void onNext(Bitmap arg0) {
                    }
                });
    }

    private boolean writeResponseBodyToDisk(ResponseBody body, String fileName) {
        try {
            File futureStudioIconFile = new File(FileOperationUtils.getExternalCacheDirectory() + fileName);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
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
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }
}

