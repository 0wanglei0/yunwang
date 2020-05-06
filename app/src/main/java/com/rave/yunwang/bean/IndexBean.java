package com.rave.yunwang.bean;

import java.io.Serializable;
import java.util.List;

public class IndexBean {

    private List<TaskBean> today;
    private List<TaskBean> other;

    public List<TaskBean> getToday() {
        return today;
    }

    public void setToday(List<TaskBean> today) {
        this.today = today;
    }

    public List<TaskBean> getOther() {
        return other;
    }

    public void setOther(List<TaskBean> other) {
        this.other = other;
    }

    public static class TaskBean implements Serializable {
        /**
         * name : 1111
         * phone : 13344556677
         * jr : 今日
         * year : 2019
         * month : 12
         * day : 24
         * task_status : 9:00-18:00
         * norecorded : 11
         * ver_fail : 0
         * recorded : 0
         * all_num : 11
         */

        private String name;
        private String phone;
        private String jr;
        private String year;
        private String month;
        private String day;
        private String task_status;
        private int norecorded;
        private int ver_fail;
        private int recorded;
        private int all_num;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getJr() {
            return jr;
        }

        public void setJr(String jr) {
            this.jr = jr;
        }

        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public String getTask_status() {
            return task_status;
        }

        public void setTask_status(String task_status) {
            this.task_status = task_status;
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

        public int getRecorded() {
            return recorded;
        }

        public void setRecorded(int recorded) {
            this.recorded = recorded;
        }

        public int getAll_num() {
            return all_num;
        }

        public void setAll_num(int all_num) {
            this.all_num = all_num;
        }
    }
}
