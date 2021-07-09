package com.ssm.demo.service;

import com.ssm.demo.entity.Goods;
import com.ssm.demo.entity.Store;
import com.ssm.demo.utils.PageResult;
import com.ssm.demo.utils.PageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 13 on 2018/7/4.
 */

public interface GoodsService {

    /**
     * 获取商品信息
     *
     * @return
     */
    PageResult getGoodsPage(PageUtil pageUtil);

    /**
     * 根据店铺获取商品名
     *
     * @return
     */
    List<Goods> getGoodsByStore(int storeId);

    /**
     * 新增商品(已有则修改)
     *
     * @return
     */
    int updateOrInsertGoods(Goods goods);

    /**
     * 检测脚本是否需要开启
     */
    void checkScript();

    /**
     * 启动固定店铺的全局脚本
     */
    void startOverallScript(Store store);

    /**
     * 启动固定店铺的优化脚本
     */
    void startOptimizingScript(Store store, ArrayList<String> storeNameList);

    /**
     * 启动固定店铺的提价脚本
     */
    void startRaisePriceScript(Store store, ArrayList<String> storeNameList);

    /**
     * 根据excel导入用户记录
     *
     * @param file
     * @return
     */
    int importUsersByExcelFile(File file);

    /**
     * 根据商品id和店铺ID获取用户记录
     *
     * @return
     */
    Goods selectByGoodsAndStore(Goods goods);

    /**
     * 插入脚本运行记录
     *
     * @return
     */
    void insertScriptRecord(Store store, int ScriptId, long startTime, int total, int success, int fail);

    /**
     * 获取商品调整价差
     *
     * @return
     */
    float getPriceDiffer();

    /**
     * 更新商品调整价差
     * @return
     */
    int updatePriceDiffer(float newPriceDiffer);
}
