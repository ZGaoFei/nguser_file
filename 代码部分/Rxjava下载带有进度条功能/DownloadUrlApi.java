package cn.com.nggirl.nguser.utils.downloadutils;


import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Url;
import rx.Observable;

public interface DownloadUrlApi {

    @GET
    Observable<ResponseBody> downloadFileFromNet(@Url String fileUrl);

}
