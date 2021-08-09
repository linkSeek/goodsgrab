package com.ssm.demo.service.impl;

import com.ssm.demo.dao.GoodsDao;
import com.ssm.demo.dao.ScriptDao;
import com.ssm.demo.dao.StoreDao;
import com.ssm.demo.entity.AdminUser;
import com.ssm.demo.entity.Goods;
import com.ssm.demo.entity.Script;
import com.ssm.demo.entity.Store;
import com.ssm.demo.service.GoodsService;
import com.ssm.demo.utils.*;
import lombok.SneakyThrows;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import sun.reflect.annotation.ExceptionProxy;

import java.io.File;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author aaron
 */
@Service("goodsService")
public class GoodsServiceImpl implements GoodsService {
    final static Logger logger = Logger.getLogger(GoodsService.class);
    @Autowired
    private GoodsDao goodsDao;

    @Autowired
    private  ScriptDao scriptDao;

    @Autowired
    private StoreDao storeDao;

    private ScriptStatusUtil scriptStatusUtil = ScriptStatusUtil.getInstance();
//    private StoreServiceImpl storeService = new StoreServiceImpl();

    @Override
    public PageResult getGoodsPage(PageUtil pageUtil) {
        List<Goods> goodsList = goodsDao.findGoodsByPages(pageUtil);
        long createTime = 0;
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for(Goods goods : goodsList) {
            createTime = goods.getCreateTime() *1000;
            goods.setCreateDate(sdf.format(new Date(createTime)));
        }
        int total = goodsDao.getTotalGoods(pageUtil);
        PageResult pageResult = new PageResult(goodsList, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public List<Goods> getGoodsByStore(int storeId) {
        return goodsDao.getAliveGoodsByStore(storeId);
    }

    @Override
    public int updateOrInsertGoods(Goods goods) {
        return goodsDao.insertGoods(goods);
    }

    @Override
    public Goods selectByGoodsAndStore(Goods goods) {
        return goodsDao.selectByGoodIdAndStore(goods);
    }

    @Override
    public float getPriceDiffer() {
        return scriptDao.getPriceDiffer();
    }

    @Override
    public int updatePriceDiffer(float newPriceDiffer) {
        return scriptDao.updatePriceDiffer(newPriceDiffer);
    }

    @Override
    public void checkScript(){
        try {
            if (!scriptStatusUtil.checkScriptRun()) {
                scriptStatusUtil.startScript();
            } else {
                return;
            }
            List<Script> scriptList = scriptDao.getAllAliveScript();
            List<Store> storeList = storeDao.getAllStore();
            long nowTime = DateUtil.now();

            HashMap<String, String> updateMap = new HashMap<>();

            ArrayList<String> storeNameList = new ArrayList<>();
            for (Store store : storeList) {
                storeNameList.add(store.getName());
            }
            for (Script script : scriptList) {
                if (nowTime > (script.getInterval() + script.getLastRunTime())) {
                    switch (script.getScriptId()) {
                        case 1:
                            updateMap.put("scriptId", "1");
                            updateMap.put("now", String.valueOf(nowTime));
                            scriptDao.updateScriptLastRunTime(updateMap);
                            for (Store store : storeList) {
                                startOverallScript(store);
                            }
                            break;
                        case 2:
                            updateMap.put("scriptId", "2");
                            updateMap.put("now", String.valueOf(nowTime));
                            scriptDao.updateScriptLastRunTime(updateMap);
                            for (Store store : storeList) {
                                startOptimizingScript(store, storeNameList);
                            }
                            break;
                        case 3:
                            updateMap.put("scriptId", "3");
                            updateMap.put("now", String.valueOf(nowTime));
                            scriptDao.updateScriptLastRunTime(updateMap);
                            for (Store store : storeList) {
                                startRaisePriceScript(store, storeNameList);
                            }
                            break;
                        default:
                    }
                }
            }
        }  catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            scriptStatusUtil.closeScript();
        }
    }
    /**
     * 启动固定店铺的全局脚本
     */
    @Override
    public void startOverallScript(Store store) {
            // 获取所有该店铺下的商品
            List<Goods> goodsList = getGoodsByStore(store.getId());
            SeleniumUtil seleniumUtil = new SeleniumUtil();
            GoodsPriceUtil goodsPriceUtil = new GoodsPriceUtil();
            int totalGoodsNumber = goodsList.size(), successGoodsNumber = 0, failGoodsNumber = 0;
            long startTime = DateUtil.now();
            // 差价
            double priceDiffer = scriptDao.getPriceDiffer();

            Map<String, Object> prefs = new HashMap<String, Object>();
            // 设置提醒的设置，2表示block
            prefs.put("profile.default_content_setting_values.notifications", 2);
            ChromeOptions options = new ChromeOptions();
            options.setExperimentalOption("prefs", prefs);
            options.addArguments("blink-settings=imagesEnabled=false");


            WebDriver driver = new ChromeDriver(options);
            Actions actions = new Actions(driver);

            // 隐式等待10s
            driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        try {
            // 登录该店铺
            seleniumUtil.noonLogin(driver, store);
            seleniumUtil.closeNotice(driver);
            seleniumUtil.pageReload(driver);

            for (Goods goods : goodsList) {
                try {
                    ExecutorService executorService = Executors.newCachedThreadPool();
                    final CountDownLatch latch = new CountDownLatch(2);
                    final double[] lowestPrice = new double[1];
                    final double[] myStorePrice = new double[1];
                    try {
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                //1.查询商品最低价
                                try {
//                                    logger.info(" 1 : " + System.currentTimeMillis() + " " + goods.getGoodsId());
                                    lowestPrice[0] = goodsPriceUtil.getGoodsLowestPrice(goods.getGoodsId());
//                                    logger.info(" 2 : " + System.currentTimeMillis() + " " + goods.getGoodsId());
                                } catch (Exception e) {
                                    logger.error(Arrays.toString(e.getStackTrace()));
                                } finally {
                                    latch.countDown();
                                }
                            }
                        });
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                //2.查询自己店铺价格
                                try {
                                    logger.info(" 3 : " + System.currentTimeMillis() + " " + goods.getGoodsId());
                                    myStorePrice[0] = seleniumUtil.getMyStorePrice(driver, actions, goods.getGoodsId());
                                    logger.info(" 4 : " + System.currentTimeMillis() + " " + goods.getGoodsId());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    logger.error(Arrays.toString(e.getStackTrace()));
                                } finally {
                                    latch.countDown();
                                }
                            }
                        });
                        // 一定记得加上timeout时间，防止阻塞主线程
                        latch.await(30000, TimeUnit.MILLISECONDS);
                    } finally {
                        //5.关闭线程池
                        executorService.shutdown();
                    }
//                    double lowestPrice = goodsPriceUtil.getGoodsLowestPrice(goods.getGoodsId());
//                    double myStorePrice = seleniumUtil.getMyStorePrice(driver, goods.getGoodsId());
                    logger.info("startOverallScript goodsId is " + goods.getGoodsId() + ", lowestPrice is " + lowestPrice[0] +
                            ", myStorePrice is " + myStorePrice[0]);
                    if(SystemUtil.doubleNumberEqual(myStorePrice[0], 0.0)) {
                        seleniumUtil.pageReload(driver);
                    }

                    if( SystemUtil.doubleNumberEqual(myStorePrice[0], 0.0) || SystemUtil.doubleNumberEqual(lowestPrice[0], 0.0) ){
                        continue;
                    }

                    double limitedPrice = goods.getLimitedPrice();
                    double allStorePrice = goods.getPrice();
                    logger.info("startOverallScript goodsId is " + goods.getGoodsId() + ", lowestPrice is " + lowestPrice[0] +
                            " , limitedPrice is " + limitedPrice + ", myStorePrice is " + myStorePrice[0] +
                            " , allStorePrice is " + allStorePrice);
                    if(SystemUtil.doubleNumberEqual(myStorePrice[0], lowestPrice[0]) && !SystemUtil.doubleNumberEqual(allStorePrice, lowestPrice[0])) {
                        goodsDao.updateGoodsPrice(goods.getGoodsId(), myStorePrice[0]);
                    }

                    if(SystemUtil.doubleNumberEqual(lowestPrice[0], allStorePrice) && !SystemUtil.doubleNumberEqual(myStorePrice[0], lowestPrice[0])) {
                        seleniumUtil.changeMyStorePrice(driver, actions, lowestPrice[0]);
                        goodsDao.updateGoodsUpdateTimes(goods);
                        successGoodsNumber ++;
                    } else if (!SystemUtil.doubleNumberEqual(lowestPrice[0], allStorePrice) && lowestPrice[0] < myStorePrice[0] && (lowestPrice[0] - priceDiffer) >= limitedPrice) {
                        seleniumUtil.changeMyStorePrice(driver, actions, lowestPrice[0] - priceDiffer);
                        goodsDao.updateGoodsUpdateTimes(goods);
                        successGoodsNumber++;
                        logger.info(" 6 : "+ System.currentTimeMillis()+" " +goods.getGoodsId());
                        goodsDao.updateGoodsPrice(goods.getGoodsId(), (double) Math.round((lowestPrice[0] - priceDiffer) * 100) / 100);
                    } else {
                        failGoodsNumber++;
                    }
                } catch (Exception e) {
                    logger.error(Arrays.toString(e.getStackTrace()));
                    // 修改价格可能出现问题，那么重新加载本页面
                    seleniumUtil.pageReload(driver);
                }
            }
        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()));
        } finally {
            // 退出浏览器
            driver.quit();
            HashMap<String,String> msgMap = new HashMap<>();
            msgMap.put("scriptId", "1");
            msgMap.put("totalGoods", String.valueOf(totalGoodsNumber) );
            msgMap.put("successGoods", String.valueOf(successGoodsNumber) );
            msgMap.put("failGoods", String.valueOf(failGoodsNumber) );
            msgMap.put("startTime", String.valueOf(startTime) );
            msgMap.put("endTime", String.valueOf(DateUtil.now()));
            msgMap.put("storeName", store.getName());
            DingTalkUtil.sendErrorMsg(msgMap);
            insertScriptRecord(store,1,startTime,totalGoodsNumber, successGoodsNumber, failGoodsNumber);
        }
    }

    /**
     * 启动固定店铺的优化脚本
     */
    @Override
    public void startOptimizingScript(Store store, ArrayList<String> storeNameList){
        // 获取所有该店铺下的商品
        List<Goods> goodsList = getGoodsByStore(store.getId());
        SeleniumUtil seleniumUtil = new SeleniumUtil();
        GoodsPriceUtil goodsPriceUtil = new GoodsPriceUtil();
        int totalGoodsNumber = goodsList.size(), successGoodsNumber = 0, failGoodsNumber = 0;
        long startTime = DateUtil.now();
        // 差价
        double priceDiffer = scriptDao.getPriceDiffer();

        Map<String, Object> prefs = new HashMap<String, Object>();
        // 设置提醒的设置，2表示block
        prefs.put("profile.default_content_setting_values.notifications", 2);
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", prefs);

        WebDriver driver = new ChromeDriver(options);
        Actions actions = new Actions(driver);

        // 隐式等待10s
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS) ;

        try {
            // 登录该店铺
            seleniumUtil.noonLogin(driver, store);
            seleniumUtil.pageReload(driver);

            for (Goods goods : goodsList) {
                try {
                    ExecutorService executorService = Executors.newCachedThreadPool();
                    final CountDownLatch latch = new CountDownLatch(2);
                    final double[] lowestPrice = new double[1];
                    final double[] myStorePrice = new double[1];
                    executorService.execute(new Runnable() {
                        @SneakyThrows
                        @Override
                        public void run() {
                            //1.查询商品最低价
                            try {
                                lowestPrice[0] = goodsPriceUtil.getGoodsLowestPrice(goods.getGoodsId());
                            }catch (Exception e) {
                                logger.info(e.getMessage());
                                throw e;
                            }
                            latch.countDown();
                        }
                    });
                    executorService.execute(new Runnable() {
                        @SneakyThrows
                        @Override
                        public void run() {
                            //2.查询自己店铺价格
                            try {
                                myStorePrice[0] = seleniumUtil.getMyStorePrice(driver,actions, goods.getGoodsId());
                            }catch (Exception e) {
                                logger.error(e.getLocalizedMessage() + e.getMessage());
                                throw e;
                            }
                            latch.countDown();
                        }
                    });
                    try {
                        // 一定记得加上timeout时间，防止阻塞主线程
                        latch.await(10000, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //5.关闭线程池
                    executorService.shutdown();

//                    double lowestPrice = goodsPriceUtil.getGoodsLowestPrice(goods.getGoodsId());
//                    System.out.println(" 2 : "+ System.currentTimeMillis());
//                    double myStorePrice = seleniumUtil.getMyStorePrice(driver, goods.getGoodsId());
//                    System.out.println(" 3 : "+ System.currentTimeMillis());
                    if(myStorePrice[0] == 0 || lowestPrice[0] ==0){
                        continue;
                    }
                    double limitedPrice = goods.getLimitedPrice();
                    if (lowestPrice[0] < myStorePrice[0]) {
                        // 获取优化方案下的修改价格
                        double revisedPrice =
                                goodsPriceUtil.getRevisedPrice(goods.getGoodsId(), store.getName(), storeNameList, priceDiffer);
                        logger.info("startOptimizingScript goodsId is " + goods.getGoodsId() + ", lowestPrice is " + lowestPrice[0] + " , limitedPrice is "
                                + limitedPrice + ", myStorePrice is " + myStorePrice[0] + " , revisedPrice is " + revisedPrice);
                        if (revisedPrice >= limitedPrice &&  !SystemUtil.doubleNumberEqual(revisedPrice ,myStorePrice[0])) {
                            seleniumUtil.changeMyStorePrice(driver, actions, revisedPrice);
                            goodsDao.updateGoodsUpdateTimes(goods);
                            successGoodsNumber++;
                        } else {
                            failGoodsNumber++;
                        }
                    } else {
                        failGoodsNumber ++;
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    seleniumUtil.pageReload(driver);
                }
            }
        }  catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            // 退出浏览器
            driver.quit();
            HashMap<String,String> msgMap = new HashMap<>();
            msgMap.put("scriptId", "2");
            msgMap.put("totalGoods", String.valueOf(totalGoodsNumber) );
            msgMap.put("successGoods", String.valueOf(successGoodsNumber) );
            msgMap.put("failGoods", String.valueOf(failGoodsNumber) );
            msgMap.put("startTime", String.valueOf(startTime) );
            msgMap.put("endTime", String.valueOf(DateUtil.now()));
            msgMap.put("storeName", store.getName());
            DingTalkUtil.sendErrorMsg(msgMap);
            insertScriptRecord(store,1,startTime,totalGoodsNumber, successGoodsNumber, failGoodsNumber);
        }
    }

    /**
     * 启动固定店铺的提价脚本
     */
    @Override
    public void startRaisePriceScript(Store store, ArrayList<String> storeNameList){
        // 获取所有该店铺下的商品
        List<Goods> goodsList = getGoodsByStore(store.getId());
        SeleniumUtil seleniumUtil = new SeleniumUtil();
        GoodsPriceUtil goodsPriceUtil = new GoodsPriceUtil();
        int totalGoodsNumber = goodsList.size(), successGoodsNumber = 0, failGoodsNumber = 0, matchCondtionNumber = 0;
        long startTime = DateUtil.now();
        // 差价
        double priceDiffer = scriptDao.getPriceDiffer();

        Map<String, Object> prefs = new HashMap<String, Object>();
        // 设置提醒的设置，2表示block
        prefs.put("profile.default_content_setting_values.notifications", 2);
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", prefs);

        WebDriver driver = new ChromeDriver(options);
        Actions actions = new Actions(driver);

        // 隐式等待10s
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS) ;
        try {

            // 登录该店铺
            seleniumUtil.noonLogin(driver, store);
            seleniumUtil.closeNotice(driver);
            seleniumUtil.pageReload(driver);

            for (Goods goods : goodsList) {
                try {
                    ExecutorService executorService = Executors.newCachedThreadPool();
                    final CountDownLatch latch = new CountDownLatch(2);
                    final double[] lowestPrice = new double[1];
                    final double[] myStorePrice = new double[1];
                    try {
                        executorService.execute(new Runnable() {
                            @SneakyThrows
                            @Override
                            public void run() {
                                //1.查询商品最低价
                                try {
                                    logger.info(" 1 : " + System.currentTimeMillis() + " " + goods.getGoodsId());
                                    lowestPrice[0] = goodsPriceUtil.getGoodsLowestPrice(goods.getGoodsId());
                                    logger.info(" 2 : " + System.currentTimeMillis() + " " + goods.getGoodsId());
                                } catch (Exception e) {
                                    logger.info(Arrays.toString(e.getStackTrace()));
                                }
                                latch.countDown();
                            }
                        });
                        executorService.execute(new Runnable() {
                            @SneakyThrows
                            @Override
                            public void run() {
                                //2.查询自己店铺价格
                                try {
                                    logger.info(" 3 : " + System.currentTimeMillis() + " " + goods.getGoodsId());
                                    myStorePrice[0] = seleniumUtil.getMyStorePrice(driver, actions, goods.getGoodsId());
                                    logger.info(" 4 : " + System.currentTimeMillis() + " " + goods.getGoodsId());
                                } catch (Exception e) {
                                    logger.info(Arrays.toString(e.getStackTrace()));
                                }
                                latch.countDown();
                            }
                        });
                        // 一定记得加上timeout时间，防止阻塞主线程
                        latch.await(30000, TimeUnit.MILLISECONDS);
                    }finally {
                        //5.关闭线程池
                        executorService.shutdown();
                    }

//                    double lowestPrice = goodsPriceUtil.getGoodsLowestPrice(goods.getGoodsId());
//                    double myStorePrice = seleniumUtil.getMyStorePrice(driver, goods.getGoodsId());

                    if(SystemUtil.doubleNumberEqual(myStorePrice[0], 0.0)) {
                        seleniumUtil.pageReload(driver);
                    }

                    if(myStorePrice[0] == 0 || lowestPrice[0] ==0){
                        continue;
                    }

                    double limitedPrice = goods.getLimitedPrice();
                    logger.info("startRaisePriceScript goodsId is " + goods.getGoodsId() + ", lowestPrice is " + lowestPrice[0] + " , limitedPrice is "
                            + limitedPrice + ", myStorePrice is " + myStorePrice[0] );

                    if (lowestPrice[0] >= myStorePrice[0] && !SystemUtil.doubleNumberEqual(lowestPrice[0], 0.0) && !SystemUtil.doubleNumberEqual(myStorePrice[0], 0.0)) {
                        logger.info(" 5 : "+ System.currentTimeMillis()+" " +goods.getGoodsId());
                        matchCondtionNumber ++;
                        // 获取优化方案下的修改价格
                        double revisedPrice =
                                goodsPriceUtil.getRevisedPrice(goods.getGoodsId(), store.getName(), storeNameList, priceDiffer);
                        logger.info(" 6 : "+ System.currentTimeMillis() + goods.getGoodsId());
                        logger.info("startRaisePriceScript goodsId is " + goods.getGoodsId() + ", lowestPrice is " + lowestPrice[0] + " , limitedPrice is "
                                + limitedPrice + ", myStorePrice is " + myStorePrice[0] + " , revisedPrice is " + revisedPrice);
                        if (revisedPrice >= limitedPrice &&  !SystemUtil.doubleNumberEqual(revisedPrice ,myStorePrice[0])) {
                            seleniumUtil.changeMyStorePrice(driver, actions, revisedPrice);
                            goodsDao.updateGoodsUpdateTimes(goods);
                            logger.info(" 7 : "+ System.currentTimeMillis()+" " +goods.getGoodsId());
                            successGoodsNumber++;
                        } else {
                            failGoodsNumber++;
                        }
                    }
                } catch (Exception e) {
                    logger.error(Arrays.toString(e.getStackTrace()));
                    seleniumUtil.pageReload(driver);
                }
            }
        }  catch (Exception e) {
            logger.info(Arrays.toString(e.getStackTrace()));
        } finally {
            // 退出浏览器
            driver.quit();
            HashMap<String,String> msgMap = new HashMap<>();
            msgMap.put("scriptId", "3");
            msgMap.put("totalGoods", String.valueOf(totalGoodsNumber) );
            msgMap.put("successGoods", String.valueOf(successGoodsNumber) );
            msgMap.put("failGoods", String.valueOf(failGoodsNumber) );
            msgMap.put("startTime", String.valueOf(startTime) );
            msgMap.put("endTime", String.valueOf(DateUtil.now()));
            msgMap.put("storeName", store.getName());
            msgMap.put("matchConditionNumber", String.valueOf(matchCondtionNumber));
            DingTalkUtil.sendErrorMsg(msgMap);
            insertScriptRecord(store,1,startTime,totalGoodsNumber, successGoodsNumber, failGoodsNumber);
        }

    }

    @Override
    public int importUsersByExcelFile(File file) {
        XSSFSheet xssfSheet = null;
        try {
            //读取file对象并转换为XSSFSheet类型对象进行处理
            xssfSheet = PoiUtil.getXSSFSheet(file);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        List<Goods> goodsList = new ArrayList<>();
        goodsDao.downAllGoods();

        HashMap<Integer, ArrayList<String>> goodsIdTwoDimArray = new HashMap<>();
        ArrayList<String> tmpArray = new ArrayList<>();

        //第一行是表头因此默认从第二行读取
        for (int rowNum = 1; rowNum <= xssfSheet.getLastRowNum(); rowNum++) {
            //按行读取数据
            XSSFRow xssfRow = xssfSheet.getRow(rowNum);
            if (xssfRow != null) {
                //实体转换
                Goods goods = convertXSSFRowToGoods(xssfRow);
                goods.setStatus(1);
                //用户验证 已存在或者为空则不进行insert操作
                if (!StringUtils.isEmpty(goods.getGoodsId()) && !StringUtils.isEmpty(goods.getStoreId())) {
                     if(selectByGoodsAndStore(goods) == null) {
                         goodsList.add(goods);
                     } else {
                         if(goodsIdTwoDimArray.get(goods.getStoreId()) == null) {
                             ArrayList<String> goodsIdArray = new ArrayList<>();
                             goodsIdArray.add(goods.getGoodsId());
                             goodsIdTwoDimArray.put(goods.getStoreId(), goodsIdArray);
                         } else {
                             tmpArray = goodsIdTwoDimArray.get(goods.getStoreId());
                             tmpArray.add(goods.getGoodsId());
                         }
                     }

                }
            }
        }
        int insertNumber = 0;

        logger.info("insertNumber " + goodsList.size() + " , downNumber is "+ goodsIdTwoDimArray);
        //判空
        if (!CollectionUtils.isEmpty(goodsList)) {
            //adminUsers用户列表不为空则执行批量添加sql
            insertNumber = goodsDao.insertGoodsBatch(goodsList);
        }

        if(!goodsIdTwoDimArray.isEmpty()) {
            for (int key: goodsIdTwoDimArray.keySet()) {
                ArrayList<String> goodsIds = goodsIdTwoDimArray.get(key);
                int result =  goodsDao.upGoodsByGoodsIds(goodsIdTwoDimArray.get(key), key);
                insertNumber = (insertNumber == 0) ? result:insertNumber;
            }
        }
        return insertNumber;
    }

    /**
     * 方法抽取
     * 将解析的列转换为AdminUser对象
     *
     * @param xssfRow
     * @return
     */
    private Goods convertXSSFRowToGoods(XSSFRow xssfRow) {
        Goods goods = new Goods();
        //商品后台ID
        XSSFCell goodBackId = xssfRow.getCell(0);
        //商品ID
        XSSFCell goodId = xssfRow.getCell(1);
        //商品名
        XSSFCell goodsName = xssfRow.getCell(2);
        //限定价格
        XSSFCell limitedPrice = xssfRow.getCell(3);
        //商品名
        XSSFCell storeId = xssfRow.getCell(4);

        try {
            //设置用户名
            if (!StringUtils.isEmpty(goodId)) {
                goods.setGoodsId(PoiUtil.getValue(goodId));
            }
            if (!StringUtils.isEmpty(goodBackId)) {
                goods.setGoodsBackId(PoiUtil.getValue(goodBackId));
            }
            if (!StringUtils.isEmpty(goodsName)) {
                goods.setGoodsName(PoiUtil.getValue(goodsName));
            }
            if (!StringUtils.isEmpty(limitedPrice)) {
                goods.setLimitedPrice(limitedPrice.getNumericCellValue());
            }
            if (!StringUtils.isEmpty(storeId)) {
                goods.setStoreId(Integer.parseInt(PoiUtil.getValue(storeId)));
            }
            goods.setCreateTime(DateUtil.now());
//            logger.info(goods);
        }catch (Exception e) {
            logger.info(e.getMessage());
        }
        return goods;
    }

    @Override
    public void insertScriptRecord(Store store, int scriptId, long startTime, int total, int success, int fail) {
        HashMap<String, String> scriptRecord = new HashMap<>();
        scriptRecord.put("scriptId", String.valueOf(scriptId));
        scriptRecord.put("storeId", String.valueOf(store.getId()));
        scriptRecord.put("startTime", String.valueOf(startTime));
        scriptRecord.put("endTime", String.valueOf(DateUtil.now()));
        scriptRecord.put("goodsTotalNumber", String.valueOf(total));
        scriptRecord.put("goodsSuccessNumber", String.valueOf(success));
        scriptRecord.put("goodsFailNumber", String.valueOf(fail));

        scriptDao.insertScriptRecord(scriptRecord);
    }

}
