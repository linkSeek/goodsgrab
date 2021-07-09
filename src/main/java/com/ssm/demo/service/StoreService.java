package com.ssm.demo.service;

import com.ssm.demo.entity.Store;
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

}
