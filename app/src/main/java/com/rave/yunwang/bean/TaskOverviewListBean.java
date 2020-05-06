package com.rave.yunwang.bean;

import java.util.List;

public class TaskOverviewListBean {

    /**
     * data : [{"vin":"LDCB13X45D2107258","status":"未录制"},{"vin":"LDCB13X45D2107267","status":"未录制"}]
     * recorded : 0
     * norecorded : 11
     * ver_fail : 0
     */

    private int recorded;
    private int norecorded;
    private int ver_fail;
    private List<DataBean> data;

    public int getRecorded() {
        return recorded;
    }

    public void setRecorded(int recorded) {
        this.recorded = recorded;
    }

    public int getNorecorded() {
        return norecorded;
    }

    public void setNorecorded(int norecorded) {
        this.norecorded = norecorded;
    }

    public int getVer_fail() {
        return ver_fail;
    }

    public void setVer_fail(int ver_fail) {
        this.ver_fail = ver_fail;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * vin : LDCB13X45D2107258
         * status : 未录制
         */

        private String vin;
        private String status;

        public String getVin() {
            return vin;
        }

        public void setVin(String vin) {
            this.vin = vin;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
