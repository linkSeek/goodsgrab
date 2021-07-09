package com.ssm.demo.service;

import com.ssm.demo.entity.Script;
import com.ssm.demo.utils.PageResult;
import com.ssm.demo.utils.PageUtil;

import java.util.List;
import java.util.Map;

/**
 *
 * @author 13
 * @date 2018/7/4
 */

public interface ScriptService {


    /**
     * 获取所有已开启脚本
     *
     * @return
     */
    List<Script> getAliveScript();
    // 获取所有脚本
    PageResult getAllScript(PageUtil pageUtil);
    // 更新脚本信息
    int updateScriptInfo(Script script);

}
