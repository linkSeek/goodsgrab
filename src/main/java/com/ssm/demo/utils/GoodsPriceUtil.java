package com.ssm.demo.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author aaron
 */
public class GoodsPriceUtil {
    private HttpClientUtil httpClientUtil = new HttpClientUtil();
    private static JSONArray proxyList  = new JSONArray();
    private static int proxyListIndex = 0;
    public static final double MAX_PRICE = 999999;
    final static Logger logger = Logger.getLogger(GoodsPriceUtil.class);

    public double getGoodsLowestPrice(String goodsId) throws Exception {
        // 两种匹配模式
        String patternStr = "<span class=\"currency\">AED </span><strong>((\\d*)(\\.)(\\d*))";
        String patternStrSecond = "<strong data-qa=\"productPrice\">((\\d*)(\\.)(\\d*))";

        // 抓取前台页面
        String goodsPage = httpClientUtil.getData("https://www.noon.com/uae-en/search?q=" + goodsId);

        // 创建 Pattern 对象
        Pattern pattern = Pattern.compile(patternStr);
        Pattern patternSecond = Pattern.compile(patternStrSecond);
        Matcher matcher = pattern.matcher(goodsPage);
        if (matcher.find( )) {
            return Double.parseDouble(matcher.group(1).replace("AED ", ""));
        } else {
            Matcher matcherSecond = patternSecond.matcher(goodsPage);
            if(matcherSecond.find()) {
                return Double.parseDouble(matcherSecond.group(1).replace("AED ", ""));
            } else {
                return 0.0;
            }
        }
    }

    public double getRevisedPrice(String goodsId, String myStoreName, ArrayList<String> storeNameArray, double priceDiffer) throws Exception {
        JSONArray offersList = getAllOffersInfo( goodsId);
        double isFbnFirstPrice = MAX_PRICE , notFbnFirstPrice = MAX_PRICE, myStorePrice = 0;
        JSONObject singleOffer;
        String storeName;
        int isFbn = 0, myStoreFbn = 3;
        double price;
        System.out.println(offersList);
        for(int i = 0 ;i < offersList.size() ; i ++) {
            singleOffer = offersList.getJSONObject(i);
            storeName = singleOffer.getString("store_name");
            isFbn = singleOffer.getInteger("is_fbn");
            price = (singleOffer.get("sale_price") == null)?singleOffer.getDouble("price"):singleOffer.getDouble("sale_price");
            if(storeName.equals(myStoreName)) {
                myStoreFbn = isFbn;
                myStorePrice = price;
                if(isFbn == 1 && isFbnFirstPrice != MAX_PRICE ) {
                    return isFbnFirstPrice - priceDiffer;
                } else if(isFbn == 0 && notFbnFirstPrice != MAX_PRICE) {
                    return notFbnFirstPrice - priceDiffer;
                }
            } else if (!storeNameArray.contains(storeName)){
                if(isFbn == 1 && isFbnFirstPrice == MAX_PRICE) {
                    isFbnFirstPrice = price;
                } else if (isFbn == 0 && notFbnFirstPrice == MAX_PRICE){
                    notFbnFirstPrice = price;
                }

                // 在自己店铺价格排位为第一时，适配一下判断
                if(myStoreFbn == 1 && isFbnFirstPrice != MAX_PRICE ) {
                    return Math.max((isFbnFirstPrice - priceDiffer), myStorePrice) ;
                } else if(myStoreFbn == 0 && notFbnFirstPrice != MAX_PRICE) {
                    return Math.max((notFbnFirstPrice - priceDiffer), myStorePrice) ;
                }
            }
        }
        return 0;
    }

    public JSONArray getAllOffersInfo( String goodsId) throws Exception{
        HashMap<String, String> proxyMap = new HashMap<String, String>(2);
        getProxy(proxyMap);
        String ip  = proxyMap.get("ip");
        String port  = proxyMap.get("port");
        String allOffersRes = httpClientUtil.getData("https://www.noon.com/_svc/catalog/api/u/" + goodsId +"/p",
                ip, Integer.parseInt(port));
        JSONObject allOffersJson = JSONObject.parseObject(allOffersRes);
        return allOffersJson.getJSONObject("product").getJSONArray("variants").getJSONObject(0).getJSONArray("offers");
    }

    public void getProxy(HashMap<String, String> proxyMap) throws Exception{
        if(proxyListIndex >= proxyList.size()) {
            getProxyList();
        }
        JSONObject proxyInfo = proxyList.getJSONObject(proxyListIndex);
        String ip = proxyInfo.getString("ip");
        String port = proxyInfo.getString("port");
        proxyListIndex ++;
        proxyMap.put("ip", ip);
        proxyMap.put("port", port);
    }

    public void getProxyList() throws Exception{
        try {
            String proxyListJson = httpClientUtil.getData("http://tiqu.py.taolop.com/getflowip?count=500&global=1&type=2&sep=1&sb=&regions=us");
            System.out.println(proxyListJson);
            JSONObject resultJson = JSONObject.parseObject(proxyListJson);
            proxyList = resultJson.getJSONArray("data");
            proxyListIndex = 0;
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
