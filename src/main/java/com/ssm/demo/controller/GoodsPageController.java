package com.ssm.demo.controller;

import com.ssm.demo.common.Constants;
import com.ssm.demo.common.Result;
import com.ssm.demo.common.ResultGenerator;
import com.ssm.demo.service.GoodsService;
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
@RequestMapping("/goods")
public class GoodsPageController {

    final static Logger logger = Logger.getLogger(GoodsPageController.class);

    @Autowired
    private GoodsService goodsService;
    /**
     * 列表
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result list(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("page")) || StringUtils.isEmpty(params.get("limit"))) {
            logger.error("请求商品列表错误，参数异常！");
            return ResultGenerator.genErrorResult(Constants.RESULT_CODE_PARAM_ERROR, "参数异常！");
        }
        //查询列表数据
        PageUtil pageUtil = new PageUtil(params);
        logger.info("请求商品列表成功，参数为 page:" + params.get("page").toString() + ",limit:" + params.get("limit").toString());
        return ResultGenerator.genSuccessResult(goodsService.getGoodsPage(pageUtil));
    }

    /**
     * 批量导入商品V1
     * <p>
     * 批量导入商品(直接导入)
     */
    @RequestMapping(value = "/importV1", method = RequestMethod.POST)
    public Result saveByExcelFileV1(@RequestParam("file") MultipartFile multipartFile) {
        File file = FileUtil.convertMultipartFileToFile(multipartFile);
        if (file == null) {
            logger.error("上传文件为空，importV2导入失败");
            return ResultGenerator.genFailResult("导入失败");
        }
        int importResult = goodsService.importUsersByExcelFile(file);
        if (importResult > 0) {
            Result result = ResultGenerator.genSuccessResult();
            result.setData(importResult);
            logger.info("importV1用户导入成功");
            return result;
        } else {
            logger.error("上传文件为空，importV1导入失败");
            return ResultGenerator.genFailResult("导入失败");
        }
    }

    @RequestMapping(value = "/priceDiffer", method = RequestMethod.GET)
    public Result getPriceDiffer() {
        float priceDiffer = goodsService.getPriceDiffer();
        HashMap<String, Float> priceDifferMap = new HashMap<>();
        priceDifferMap.put("price_differ", priceDiffer);
        return ResultGenerator.genSuccessResult(priceDifferMap);
    }

    @RequestMapping(value = "/updatePriceDiffer", method = RequestMethod.POST)
    public Result updatePriceDiffer(@RequestBody Map<String, String> params) {
        float newPriceDiffer = Float.parseFloat(params.get("newPriceDiffer"));
        goodsService.updatePriceDiffer(newPriceDiffer);
        return ResultGenerator.genSuccessResult();
    }
}
