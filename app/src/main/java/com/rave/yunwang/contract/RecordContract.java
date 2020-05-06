package com.rave.yunwang.contract;

import com.rave.yunwang.base.BaseContract;
import com.rave.yunwang.bean.UserInfoBean;

import java.io.File;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class RecordContract {

    public interface View extends BaseContract.BaseView {

        String getRecordTaskId();

        UserInfoBean getUserInfo();

        String getVinCode();

        String getS2iCode1();

        String getS2iCode2();

        int getOrder();

        void initProcess(int order);

        void nextProcess();

        String getProcessTips();

        void clearLocalFile();

        void finishActivity();
    }

    public interface Presenter<T> extends BaseContract.BasePresenter<T> {

        void validVinCode(String imagePath);

        void uploadImage(boolean isUploadVinPic, String imagePath);

        void uploadVideo(String videoPath);

        void uploadS2i();

        void resetProcess();

        RequestBody createTextRequestBody(String text);

        MultipartBody.Part createVinImageFileRequestPart(File file);

        MultipartBody.Part createImageFileRequestPart(File file);

        MultipartBody.Part createVideoFileRequestPart(File file);

    }

}
