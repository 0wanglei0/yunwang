package com.rave.yunwang.bean;

import com.google.gson.annotations.SerializedName;

public class TodayRecordVideoBean {

    /**
     * task_id : 45919
     * vin : LDCB13X45D2107264
     * 3curl : null
     */

    private int task_id;
    private String vin;
    @SerializedName("3curl")
    private Object _$3curl;

    public int getTask_id() {
        return task_id;
    }

    public void setTask_id(int task_id) {
        this.task_id = task_id;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public Object get_$3curl() {
        return _$3curl;
    }

    public void set_$3curl(Object _$3curl) {
        this._$3curl = _$3curl;
    }
}
