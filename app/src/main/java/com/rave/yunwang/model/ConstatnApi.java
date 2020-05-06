package com.rave.yunwang.model;

import okhttp3.MediaType;

/**
 * 作者：tianrenzheng on 2019/12/16 13:50
 * 邮箱：317642600@qq.com
 */
public class ConstatnApi {
    public static final String API_BASE_URL = "http://47.92.102.48/yunyan/public/index.php/api/";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType TEXT = MediaType.parse("text/plain; charset=utf-8");
    public static final MediaType Form = MediaType.parse("multipart/form-data");
}
