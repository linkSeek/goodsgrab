package com.ssm.demo.service.impl;

import com.ssm.demo.dao.StoreDao;
import com.ssm.demo.entity.Goods;
import com.ssm.demo.entity.Store;
import com.ssm.demo.service.StoreService;
import com.ssm.demo.utils.PageResult;
import com.ssm.demo.utils.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author aaron
 */
@Service("storeService")
public class StoreServiceImpl implements StoreService {
    @Autowired
    private StoreDao storeDao;

    @Override
    public List<Store> getAllStore() {
        List<Store> storeList = storeDao.getAllStore();
        return storeList;
    }

    @Override
    public int insertStore(Store store){
        return storeDao.insertStore(store);
    }

    @Override
    public int updateStorePassword(Store store) {
        return storeDao.updateStorePassword(store);
    }

    @Override
    public PageResult getStorePage(PageUtil pageUtil) {
        List<Store> storeList = storeDao.findStoreByPages(pageUtil);
        long createTime = 0;
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for(Store store : storeList) {
            createTime = store.getCreateTime() *1000;
            store.setCreateDate(sdf.format(new Date(createTime)));
        }
        // todo: 目前total暂不取值，定为10
        int total = 10;
        PageResult storeResult = new PageResult(storeList, total, pageUtil.getLimit(), pageUtil.getPage());
        return storeResult;
    }
}
