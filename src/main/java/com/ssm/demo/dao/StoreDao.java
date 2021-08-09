package com.ssm.demo.dao;

import com.ssm.demo.entity.Goods;
import com.ssm.demo.entity.Store;

import java.util.List;
import java.util.Map;

/**
 * @author 13
 * @date 2018-08-15
 */
public interface StoreDao {
    /**
     * 返回相应的数据集合
     *
     * @return
     */
    List<Store> getAllStore();

    /**
     * 插入新店铺
     *
     * @return
     */
    int insertStore( Store store);

    /**
     * 更新店铺密码
     *
     * @return
     */
    int updateStorePassword(Store store);

    /**
     * 返回相应的数据集合
     *
     * @param map
     * @return
     */
    List<Store> findStoreByPages(Map<String, Object> map);


}
