package com.rave.yunwang.presenter;

import android.util.Log;

import com.rave.yunwang.R;
import com.rave.yunwang.base.RxBasePresenter;
import com.rave.yunwang.bean.BaseBean;
import com.rave.yunwang.bean.OrderBean;
import com.rave.yunwang.bean.VinCodeValidResultBean;
import com.rave.yunwang.contract.RecordContract;
import com.rave.yunwang.model.RemoteRepository;
import com.rave.yunwang.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class RecordPresenter extends RxBasePresenter<RecordContract.View> implements RecordContract.Presenter<RecordContract.View> {

    @Override
    public void validVinCode(final String imagePath) {//验证vin码
        File imageFile = new File(imagePath);
        String type = "Android";
        if (!imageFile.exists()) {
            mView.showErrorTips("图片不存在");
            return;
        }

        MultipartBody.Part body = createVinImageFileRequestPart(imageFile);

        addSubscrebe(RemoteRepository.getInstance()
                .validVinCode("http://120.53.7.166:5001/vinocr", body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<VinCodeValidResultBean>() {
                    @Override
                    public void accept(VinCodeValidResultBean baseBean) throws IOException {
                        mView.setCallBackCount();
                        if (baseBean != null && !StringUtils.isEmpty(baseBean.getVin())) {
                            if (baseBean.getVin().equals(mView.getVinCode())) {
                                mView.showErrorTips(mView.getVinCode()+":vin码验证成功");
                                mCompositeDisposable.clear();
                                uploadImage(true, imagePath);
                            }
                        } else {
                            mView.showErrorTips("vin码验证失败");
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        mView.setCallBackCount();
                        Log.e("Aaaa","错误信息："+throwable.getMessage());
                        if(throwable.getMessage().equals("timeout")) {
                            mView.showErrorTips("请求超时");
                        }
                        else
                            mView.showErrorTips(throwable.getMessage());
                    }
                }));
    }

    @Override
    public void uploadImage(final boolean isUploadVinPic, String imagePath) {//上传相片
        String taskId = mView.getRecordTaskId();
        String userId = mView.getUserInfo().getUser_id() + "";
        String vinCode = mView.getVinCode();
        File imageFile = new File(imagePath);
        String type = "Android";
        if (!imageFile.exists()) {
            mView.showErrorTips("图片不存在");
            return;
        }

        RequestBody taskIdBody = createTextRequestBody(taskId);
        RequestBody userIdBody = createTextRequestBody(userId);
        RequestBody vinCodeBody = createTextRequestBody(vinCode);
        RequestBody typeBody = createTextRequestBody(type);
        MultipartBody.Part body = createImageFileRequestPart(imageFile);

        addSubscrebe(RemoteRepository.getInstance()
                .upLoadImage4(taskIdBody, userIdBody, vinCodeBody, typeBody, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean<OrderBean>>() {
                    @Override
                    public void accept(BaseBean<OrderBean> baseBean) throws IOException {
                        if (baseBean.getCode() == 0) {
                            mView.showErrorTips(R.string.success_upload_file);//上传成功
                            if (isUploadVinPic) {
                                mView.initProcess(baseBean.getResult().getOrder());
                            }
                            mView.nextProcess();
                            mCompositeDisposable.clear();
                        } else {
                            mView.showErrorTips(baseBean.getMessage());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        mView.showErrorTips(R.string.fail_upload_file);//上传失败
                    }
                }));
    }

    @Override
    public void uploadVideo(String videoPath) {//上传视频
        String taskId = mView.getRecordTaskId();
        String userId = mView.getUserInfo().getUser_id() + "";
        String vinCode = mView.getVinCode();
        int order = mView.getOrder();
        File imageFile = new File(videoPath);
        if (!imageFile.exists()) {
            mView.showErrorTips("视频文件不存在");
            return;
        }

        RequestBody taskIdBody = createTextRequestBody(taskId);
        RequestBody userIdBody = createTextRequestBody(userId);
        RequestBody vinCodeBody = createTextRequestBody(vinCode);
        RequestBody orderBody = createTextRequestBody(order + "");
        MultipartBody.Part body = createVideoFileRequestPart(imageFile);

        addSubscrebe(RemoteRepository.getInstance()
                .uploadVideo(taskIdBody, userIdBody, vinCodeBody, orderBody, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean>() {
                    @Override
                    public void accept(BaseBean baseBean) {
                        if (baseBean.getCode() == 0) {
                            mView.showErrorTips("视频上传成功");//上传成功
                            mView.clearLocalFile();
                            mView.finishActivity();
                        } else {
                            mView.showErrorTips(baseBean.getMessage());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        mView.showErrorTips("视频"+R.string.fail_upload_file);//上传失败
                    }
                }));
    }

    @Override
    public void uploadS2i() {
        String taskId = mView.getRecordTaskId();
        String userId = mView.getUserInfo().getUser_id() + "";
        String vinCode = mView.getVinCode();
        String s2icode1 = mView.getS2iCode1();
        String s2icode = mView.getS2iCode2();
        final Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("task_id", taskId);
        requestMap.put("user_id", userId);
        requestMap.put("vin", vinCode);
        requestMap.put("s2icode1", s2icode1);
        requestMap.put("s2icode", s2icode1);
        addSubscrebe(RemoteRepository.getInstance()
                .uploadS2i(requestMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean>() {
                    @Override
                    public void accept(BaseBean baseBean) throws Exception {
                        if (baseBean.getCode() == 0) {
                            mView.showErrorTips("s2i上传成功");//上传成功
                        } else {
                            mView.showErrorTips(baseBean.getMessage());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mView.showErrorTips(R.string.fail_upload_file);//上传失败
                    }
                })
        );
    }


    @Override
    public void resetProcess() {
        String taskId = mView.getRecordTaskId();
        String userId = mView.getUserInfo().getUser_id() + "";
        String vinCode = mView.getVinCode();

        final Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("task_id", taskId);
        requestMap.put("user_id", userId);
        requestMap.put("vin", vinCode);

        addSubscrebe(RemoteRepository.getInstance()
                .resetRecord(requestMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseBean>() {
                    @Override
                    public void accept(BaseBean baseBean) throws Exception {
                        if (baseBean.getCode() == 0) {
                            mView.showErrorTips(R.string.success_upload_file);//上传成功
                            mView.clearLocalFile();
                        } else {
                            mView.showErrorTips(baseBean.getMessage());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mView.showErrorTips(R.string.fail_upload_file);//上传失败
                    }
                })
        );
    }

    @Override
    public RequestBody createTextRequestBody(String text) {
        return RequestBody.create(MediaType.parse("multipart/form-data"), text);
    }

    public RequestBody createTextRequestBody(File file) {
        return RequestBody.create(MediaType.parse("multipart/form-data"), file);
    }

    @Override
    public MultipartBody.Part createVinImageFileRequestPart(File file) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        // 3.文件上传 多媒体对象
        //        RequestBody requestFile = RequestBody.create(MediaType.parse("application/octet-stream"), file);
//        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
//        MultipartBody.Part filePart = MultipartBody.Part.createFormData("img", file.getName(), requestFile);
        return MultipartBody.Part.createFormData("file",file.getName(),requestBody);
    }

    @Override
    public MultipartBody.Part createImageFileRequestPart(File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
        return filePart;
    }

    @Override
    public MultipartBody.Part createVideoFileRequestPart(File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("video/mp4"), file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("video", file.getName(), requestFile);
        return filePart;
    }
}
