package com.rave.yunwang.bean;

public class UserInfoBean {
    /**
     * {
     * 	"code": 0,
     * 	"message": "登录成功",
     * 	"result": {
     * 		"user_id": 765,
     * 		"account_num": "13344556677",
     * 		"password": "0192023a7bbd73250516f069df18b500",
     * 		"username": "11",
     * 		"address": "30.604877,104.076447",
     * 		"email": "111111@qq.com",
     * 		"phone": "13344556677"
     *        }
     * }
     */
    private int user_id;
    private String account_num;
    private String password;
    private String username;
    private String address;
    private String email;
    private String phone;

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getAccount_num() {
        return account_num;
    }

    public void setAccount_num(String account_num) {
        this.account_num = account_num;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
