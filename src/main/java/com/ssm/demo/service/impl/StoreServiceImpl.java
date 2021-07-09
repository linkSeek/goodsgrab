package com.ssm.demo.service.impl;

import com.ssm.demo.dao.StoreDao;
import com.ssm.demo.entity.Store;
import com.ssm.demo.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
