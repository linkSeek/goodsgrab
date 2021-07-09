package com.ssm.demo.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author 13
 * @date 2018/6/27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Script implements Serializable {

    /**
     * 脚本ID
     */
    private int scriptId;

    /**
     * 状态
     */
    private int status;

    /**
     * 间隔时间
     */
    private long interval;

    /**
     * 上次运行时间
     */
    private long lastRunTime;

    /**
     * 脚本描述
     */
    private String comment;


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
