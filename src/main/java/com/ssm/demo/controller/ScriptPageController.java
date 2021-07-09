package com.ssm.demo.controller;

import com.ssm.demo.common.Constants;
import com.ssm.demo.common.Result;
import com.ssm.demo.common.ResultGenerator;
import com.ssm.demo.controller.annotation.TokenToUser;
import com.ssm.demo.entity.AdminUser;
import com.ssm.demo.entity.Script;
import com.ssm.demo.service.GoodsService;
import com.ssm.demo.service.ScriptService;
import com.ssm.demo.utils.FileUtil;
import com.ssm.demo.utils.PageResult;
import com.ssm.demo.utils.PageUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

/**
 *
 * @author 13
 * @date 2021/6/21
 */
@RestController
@RequestMapping("/script")
public class ScriptPageController {

    final static Logger logger = Logger.getLogger(ScriptPageController.class);

    @Autowired
    private ScriptService scriptService;
    /**
     * 列表
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result list(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("page")) || StringUtils.isEmpty(params.get("limit"))) {
            logger.error("请求脚本列表错误，参数异常！");
            return ResultGenerator.genErrorResult(Constants.RESULT_CODE_PARAM_ERROR, "参数异常！");
        }
        //查询列表数据
        PageUtil pageUtil = new PageUtil(params);
        logger.info("请求脚本列表成功，参数为 page:" + params.get("page").toString() + ",limit:" + params.get("limit").toString());
        return ResultGenerator.genSuccessResult(scriptService.getAllScript(pageUtil));
    }

    /**
     * 批量导入商品V1
     * <p>
     * 批量导入商品(直接导入)
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Map<String, String> params) {
        Script script = new Script();
        script.setScriptId( Integer.parseInt(params.get("scriptId")));
        script.setInterval( Long.parseLong(params.get("interval")));
        script.setStatus( Integer.parseInt(params.get("status")));
        if (StringUtils.isEmpty(script.getScriptId())) {
            logger.error("修改失败");
            return ResultGenerator.genErrorResult(Constants.RESULT_CODE_PARAM_ERROR, "修改失败，请联系管理员");
        }
        int result = scriptService.updateScriptInfo(script);
        if (result > 0) {
            logger.info("请求修改脚本配置成功 " + script.toString());
            return ResultGenerator.genSuccessResult();
        } else {
            logger.info("请求修改脚本配置失败 " + script.toString());
            return ResultGenerator.genFailResult("修改失败");
        }
    }
}
