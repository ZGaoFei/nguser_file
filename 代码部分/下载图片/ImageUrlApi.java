package cn.com.nggirl.nguser.mall.business.protocol;


import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Url;
import rx.Observable;

public interface ImageUrlApi {

    @GET
    Observable<ResponseBody> downloadPicFromNet(@Url String fileUrl);
}
