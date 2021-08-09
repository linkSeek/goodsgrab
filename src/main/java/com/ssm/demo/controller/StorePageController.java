package com.ssm.demo.controller;

import com.ssm.demo.common.Constants;
import com.ssm.demo.common.Result;
import com.ssm.demo.common.ResultGenerator;
import com.ssm.demo.entity.Store;
import com.ssm.demo.service.StoreService;
import com.ssm.demo.utils.DateUtil;
import com.ssm.demo.utils.FileUtil;
import com.ssm.demo.utils.PageResult;
import com.ssm.demo.utils.PageUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author 13
 * @date 2021/6/21
 */
@RestController
@RequestMapping("/store")
public class StorePageController {

    final static Logger logger = Logger.getLogger(StorePageController.class);

    @Autowired
    private StoreService storeService;
    /**
     * 列表
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result list(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("page")) || StringUtils.isEmpty(params.get("limit"))) {
            logger.error("请求店铺列表错误，参数异常！");
            return ResultGenerator.genErrorResult(Constants.RESULT_CODE_PARAM_ERROR, "参数异常！");
        }
        //查询列表数据
        PageUtil pageUtil = new PageUtil(params);
        logger.info("请求店铺列表成功，参数为 page:" + params.get("page").toString() + ",limit:" + params.get("limit").toString());
        return ResultGenerator.genSuccessResult(storeService.getStorePage(pageUtil));
    }

    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    public Result updateStorePassword(@RequestBody Map<String, String> params) {
        Store store = new Store();
        store.setId(Integer.parseInt(params.get("id")));
        store.setPassword(params.get("password"));
        return ResultGenerator.genSuccessResult(storeService.updateStorePassword(store));
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public Result updatePriceDiffer(@RequestBody Map<String, String> params) {
        Store store = new Store();
        store.setName(params.get("name"));
        store.setEmail(params.get("email"));
        store.setPassword(params.get("password"));
        store.setCreateTime(DateUtil.now());
        return ResultGenerator.genSuccessResult(storeService.insertStore(store));
    }
}
