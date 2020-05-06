package com.rave.yunwang.model;


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

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

/**
 * 作者：tianrenzheng on 2019/12/16 13:16
 * 邮箱：317642600@qq.com
 */
public interface ApiService {

    /**
     * 检测新版本
     */
    @FormUrlEncoded
    @POST("user/check_ver")
    Observable<BaseBean> checkVersion(@FieldMap Map<String, Object> map);

    /**
     * 修改密码
     */
    @FormUrlEncoded
    @POST("user/modify_password")
    Observable<BaseBean<UserInfoBean>> modifyPassword(@FieldMap Map<String, Object> map);

    /**
     * 修改邮箱
     */
    @FormUrlEncoded
    @POST("user/modify_email")
    Observable<BaseBean<UserInfoBean>> modifyEmail(@FieldMap Map<String, Object> map);

    /**
     * 修改电话号码
     */
    @FormUrlEncoded
    @POST("user/modify_phone")
    Observable<BaseBean<UserInfoBean>> modifyPhoneNum(@FieldMap Map<String, Object> map);

    /**
     * 登录
     */
    @FormUrlEncoded
    @POST("login/login")
    Observable<BaseBean<UserInfoBean>> login(@FieldMap Map<String, Object> map);

    /**
     * 登录
     */
    @FormUrlEncoded
    @POST("login/insert_address")
    Observable<BaseBean<UserInfoBean>> insertAddress(@FieldMap Map<String, Object> map);

    /**
     * 登出
     */
    @FormUrlEncoded
    @POST("login/login_out")
    Observable<BaseBean> logout(@FieldMap Map<String, Object> map);

    /**
     * 获取首页数据
     *
     * @param map
     * @return
     */
    @FormUrlEncoded
    @POST("index/index")
    Observable<BaseBean<IndexBean>> getTaskList(@FieldMap Map<String, Object> map);

    /**
     * 联想搜索
     *
     * @param map
     * @return
     */
    @FormUrlEncoded
    @POST("record/associat")
    Observable<BaseBean<List<AssociatBean>>> getAssociatedList(@FieldMap Map<String, Object> map);

    /**
     * 获取已录制的任务列表
     *
     * @param map
     * @return
     */
    @FormUrlEncoded
    @POST("index/recorded")
    Observable<BaseBean<TaskOverviewListBean>> getRecordedTaskList(@FieldMap Map<String, Object> map);


    /**
     * 获取未录制的任务列表
     *
     * @param map
     * @return
     */
    @FormUrlEncoded
    @POST("index/norecorded")
    Observable<BaseBean<TaskOverviewListBean>> getNotRecordedTaskList(@FieldMap Map<String, Object> map);

    /**
     * 获取验证失败的任务列表
     *
     * @param map
     * @return
     */
    @FormUrlEncoded
    @POST("index/ver_fail")
    Observable<BaseBean<TaskOverviewListBean>> getVerifyFailTaskList(@FieldMap Map<String, Object> map);

    /**
     * 获取验证失败的任务列表
     *
     * @param map
     * @return
     */
    @FormUrlEncoded
    @POST("index/all_today_vin")
    Observable<BaseBean<List<TodayRecordVideoBean>>> getAllTodayVin(@FieldMap Map<String, Object> map);

    /**
     * 车在途按钮
     *
     * @param map
     * @return
     */
    @FormUrlEncoded
    @POST("index/on_the_way")
    Observable<BaseBean> requestOnTheWay(@FieldMap Map<String, Object> map);

    /**
     * 上传照片(Vin码、车头照、门店Logo场景、卡片背面上传)
     *
     * @param taskId
     * @param userId
     * @param vinCode
     * @param type
     * @param file
     * @return
     */
    @Multipart
    @POST("record/uploadimage")
    Observable<BaseBean<OrderBean>> upLoadImage4(@Part("task_id") RequestBody taskId,
                                                 @Part("user_id") RequestBody userId,
                                                 @Part("vin") RequestBody vinCode,
                                                 @Part("type") RequestBody type,
                                                 @Part MultipartBody.Part file);


    /**
     * 车侧面玻璃上传
     *
     * @param taskId
     * @param userId
     * @param vinCode
     * @param file
     * @return
     */
    @Multipart
    @POST("record/upload_3cp")
    Observable<BaseBean> upload3CPicture(@Part("task_id") RequestBody taskId,
                                         @Part("user_id") RequestBody userId,
                                         @Part("vin") RequestBody vinCode,
                                         @Part MultipartBody.Part file);

    /**
     * 视频上传
     *
     * @param taskId
     * @param userId
     * @param vinCode
     * @param order
     * @param file
     * @return
     */
    @Multipart
    @POST("record/uploadVideo")
    Observable<BaseBean> uploadVideo(@Part("task_id") RequestBody taskId,
                                     @Part("user_id") RequestBody userId,
                                     @Part("vin") RequestBody vinCode,
                                     @Part("order") RequestBody order,
                                     @Part MultipartBody.Part file);

    /**
     * 重置拍摄流程
     *
     * @param params
     * @return
     */
    @FormUrlEncoded
    @POST("record/reset")
    Observable<BaseBean> resetRecord(@FieldMap Map<String, Object> params);


    /**
     * 验证vin码
     *
     * @param url
     * @param vinImage
     * @return
     */
    @Multipart
    @POST
    Observable<VinCodeValidResultBean> validVinCode(@Url String url, @Part MultipartBody.Part vinImage);

    /**
     * 上传S2i
     *
     * @param params
     * @return
     */
    @FormUrlEncoded
    @POST("index/stoicode")
    Observable<BaseBean> uploadS2i(@FieldMap Map<String, Object> params);


}
