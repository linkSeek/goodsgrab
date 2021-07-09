package com.ssm.demo.entity;

import java.io.Serializable;
//import java.util.Date;
import lombok.Data;

/**
 *
 * @author 13
 * @date 2018/6/27
 */
@Data
public class Store implements Serializable {

    /**
     * ID
     */
    private int id;

    /**
     * 店铺名
     */
    private String name;

    /**
     * 邮件地址
     */
    private String email;

    /**
     * 密码
     */
    private String password;

    /**
     * 创建时间
     */
    private String createTime;


//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getUserName() {
//        return userName;
//    }
//
//    public void setUserName(String userName) {
//        this.userName = userName;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
//
//    public String getUserToken() {
//        return userToken;
//    }
//
//    public void setUserToken(String userToken) {
//        this.userToken = userToken;
//    }
//
//    public int getIsDeleted() {
//        return isDeleted;
//    }
//
//    public void setIsDeleted(int isDeleted) {
//        this.isDeleted = isDeleted;
//    }
//
//    public Date getCreateTime() {
//        return createTime;
//    }
//
//    public void setCreateTime(Date createTime) {
//        this.createTime = createTime;
//    }
}
