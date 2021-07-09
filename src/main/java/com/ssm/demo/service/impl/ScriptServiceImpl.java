package com.ssm.demo.service.impl;

import com.ssm.demo.dao.ScriptDao;
import com.ssm.demo.entity.Script;
import com.ssm.demo.service.ScriptService;
import com.ssm.demo.utils.PageResult;
import com.ssm.demo.utils.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author aaron
 */
@Service("scriptService")
public class ScriptServiceImpl implements ScriptService {

    @Autowired
    private ScriptDao scriptDao;

    @Override
    public List<Script> getAliveScript() {
        List<Script> scriptList = scriptDao.getAllAliveScript();
        return scriptList;
    }

    @Override
    public PageResult getAllScript(PageUtil pageUtil) {
        List<Script> scriptList = scriptDao.getAllScript();
        int total = 3;
        PageResult pageResult = new PageResult(scriptList, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public int updateScriptInfo(Script script) {
        int updateResult = scriptDao.updateScriptInfo(script);
        return updateResult;
    }
}
