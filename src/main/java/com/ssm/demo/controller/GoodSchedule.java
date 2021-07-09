package com.ssm.demo.controller;

import com.ssm.demo.service.GoodsService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author aaron
 */
@Controller
public class GoodSchedule {
    final static Logger logger = Logger.getLogger(AdminUserControler.class);

    @Autowired
    private GoodsService goodsService;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void task(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        logger.info(date + " 定时任务检测 ");
        goodsService.checkScript();
    }

//    public void
}
