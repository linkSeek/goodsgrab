package com.ssm.demo.service;

import com.ssm.demo.entity.Store;
import com.ssm.demo.utils.PageResult;
import com.ssm.demo.utils.PageUtil;

import java.util.List;

/**
 * Created by 13 on 2018/7/4.
 */

public interface StoreService {


    /**
     * 获取所有店铺信息
     *
     * @return
     */
    List<Store> getAllStore();

    /**
     * 插入新店铺
     *
     * @return
     */
    int insertStore(Store store);

    /**
     * 更新店铺密码
     *
     * @return
     */
    int updateStorePassword(Store store);

    /**
     * 获取商品信息
     *
     * @return
     */
    PageResult getStorePage(PageUtil pageUtil);

}
