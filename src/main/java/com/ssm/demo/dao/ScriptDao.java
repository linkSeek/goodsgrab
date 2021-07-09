package com.ssm.demo.dao;

import com.ssm.demo.entity.Script;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 13
 * @date 2018-08-15
 */
public interface ScriptDao {
    /**
     * 获取已开启脚本
     *
     * @return
     */
    List<Script> getAllAliveScript();

    /**
     * 更新脚本运行时间
     *
     * @return
     */
    int updateScriptLastRunTime(HashMap<String, String> updateMap);

    /**
     * 插入脚本记录
     *
     * @return
     */
    int insertScriptRecord(Map<String, String> scriptRecord);

    /**
     * 获取所有脚本
     *
     * @return
     */
    List<Script> getAllScript();

    /**
     * 更新脚本配置
     *
     * @return
     */
    int updateScriptInfo(Script script);

    /**
     * 获取商品调整的价差
     *
     * @return
     */
    float getPriceDiffer();

    /**
     * 更新商品调整的价差
     *
     * @return
     */
    int updatePriceDiffer(float priceDiffer);
}
