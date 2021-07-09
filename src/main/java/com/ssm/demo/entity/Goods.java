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
public class Goods implements Serializable {

    /**
     * 商品ID
     */
    private String goodsId;

    /**
     * 商品后台ID
     */
    private String goodsBackId;

    /**
     * 商品名
     */
    private String goodsName;

    /**
     * 限定价格
     */
    private double limitedPrice;

    /**
     * 价格
     */
    private double price;

    /**
     * 店铺ID
     */
    private int storeId;

    /**
     * 店铺名
     */
    private String storeName;

    /**
     * 创建时间
     */
    private long createTime;

    /**
     * 更新时间
     */
    private long updateTime;

    /**
     * 更新次数
     */
    private long numberOfUpdate;

    private String createDate;

    private int status;

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
