package com.rave.yunwang.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.rave.yunwang.bean.AssociatBean;
import com.rave.yunwang.bean.BaseBean;
import com.rave.yunwang.bean.IndexBean;
import com.rave.yunwang.bean.OrderBean;
import com.rave.yunwang.bean.TaskOverviewListBean;
import com.rave.yunwang.bean.TodayRecordVideoBean;
import com.rave.yunwang.bean.UserInfoBean;
import com.rave.yunwang.bean.VinCodeValidResultBean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.FieldMap;

/**
 * Created by Administrator on 2017/11/23 0023.
 */
public class RemoteRepository implements ApiService {
    public static final int SECCESS_CODE = 1;
    private static RemoteRepository instance;

    private ApiService service;
    private static OkHttpClient.Builder builder;

    private RemoteRepository() {
        if (builder == null) {
            builder = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .connectTimeout(20 * 1000, TimeUnit.MILLISECONDS)
                    .readTimeout(20 * 1000, TimeUnit.MILLISECONDS)
                    .retryOnConnectionFailure(true); // 失败重发
        }

        builder.interceptors().clear();
//        builder.interceptors().add(new AddCookiesInterceptor(user));

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConstatnApi.API_BASE_URL)//设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create(gson))//设置数据解析器
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(builder.build())
                .build();

        service = retrofit.create(ApiService.class);
    }

    public static RemoteRepository getInstance() {
        instance = new RemoteRepository();
        return instance;
    }


    @Override
    public Observable<BaseBean> checkVersion(@FieldMap Map<String, Object> map) {
        return service.checkVersion(map);
    }

    @Override
    public Observable<BaseBean<UserInfoBean>> modifyPassword(Map<String, Object> map) {
        return service.modifyPassword(map);
    }

    @Override
    public Observable<BaseBean<UserInfoBean>> modifyEmail(Map<String, Object> map) {
        return service.modifyEmail(map);
    }

    @Override
    public Observable<BaseBean<UserInfoBean>> modifyPhoneNum(Map<String, Object> map) {
        return service.modifyPhoneNum(map);
    }

    @Override
    public Observable<BaseBean<UserInfoBean>> login(@FieldMap Map<String, Object> map) {
        return service.login(map);
    }

    @Override
    public Observable<BaseBean<UserInfoBean>> insertAddress(@FieldMap Map<String, Object> map) {
        return service.insertAddress(map);
    }

    @Override
    public Observable<BaseBean> logout(@FieldMap Map<String, Object> map) {
        return service.logout(map);
    }

    public Observable<BaseBean<IndexBean>> getTaskList(@FieldMap Map<String, Object> map) {
        return service.getTaskList(map);
    }
/*
* 搜索联想
* */
    @Override
    public Observable<BaseBean<List<AssociatBean>>> getAssociatedList(Map<String, Object> map) {
        return service.getAssociatedList(map);
    }

    @Override
    public Observable<BaseBean<TaskOverviewListBean>> getRecordedTaskList(Map<String, Object> map) {
        return service.getRecordedTaskList(map);
    }

    @Override
    public Observable<BaseBean<TaskOverviewListBean>> getNotRecordedTaskList(Map<String, Object> map) {
        return service.getNotRecordedTaskList(map);
    }

    @Override
    public Observable<BaseBean<TaskOverviewListBean>> getVerifyFailTaskList(Map<String, Object> map) {
        return service.getVerifyFailTaskList(map);
    }

    @Override
    public Observable<BaseBean<List<TodayRecordVideoBean>>> getAllTodayVin(Map<String, Object> map) {
        return service.getAllTodayVin(map);
    }

    @Override
    public Observable<BaseBean> requestOnTheWay(Map<String, Object> map) {
        return service.requestOnTheWay(map);
    }

    @Override
    public Observable<BaseBean<OrderBean>> upLoadImage4(RequestBody taskId, RequestBody userId, RequestBody vinCode, RequestBody type, MultipartBody.Part file) {
        return service.upLoadImage4(taskId, userId, vinCode, type, file);
    }

    @Override
    public Observable<BaseBean> upload3CPicture(RequestBody taskId, RequestBody userId, RequestBody vinCode, MultipartBody.Part file) {
        return service.upload3CPicture(taskId, userId, vinCode, file);
    }

    @Override
    public Observable<BaseBean> uploadVideo(RequestBody taskId, RequestBody userId, RequestBody vinCode, RequestBody order, MultipartBody.Part file) {
        return service.uploadVideo(taskId, userId, vinCode, order, file);
    }

    @Override
    public Observable<BaseBean> resetRecord(Map<String, Object> params) {
        return service.resetRecord(params);
    }

    @Override
    public Observable<VinCodeValidResultBean> validVinCode(String url, MultipartBody.Part vinImage) {
        return service.validVinCode(url, vinImage);
    }

    /**
     * 上传S2i
     *
     * @param params
     * @return
     */
    @Override
    public Observable<BaseBean> uploadS2i(Map<String, Object> params) {
        return service.uploadS2i(params);
    }
}
