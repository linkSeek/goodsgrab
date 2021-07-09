package com.ssm.demo.utils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.Date;
import java.util.HashMap;

public class DingTalkUtil {
    public static String WEBHOOK = "https://oapi.dingtalk.com/robot/send?access_token=7fa0225efdfbd3fef1eb2b1ad6841438cefd6c8f9dcb26f0b98e524ccf3fd2ee";
    public static String[] scriptArray = {"","全局脚本","优化脚本", "提价脚本"};
    public static String msgZero = "【";
    public static String msgOne = "】"+"运行结果，时间：";
    public static String msgTwo = ", 共检测商品";
    public static String msgThree = "件，其中成功修改商品价格";
    public static String msgFour = "件，未修改商品";
    public static String msgFive = "件，用时";
    public static String msgSix = "分";
    public static String msgSeven = "秒";
    public static String msgEight = "，其中符合条件商品数为";

    private static void sendDingTalkMsg(String msg) throws Exception{

        HttpClient httpclient = HttpClients.createDefault();

        HttpPost httppost = new HttpPost(WEBHOOK);
        httppost.addHeader("Content-Type", "application/json; charset=utf-8");

        String textMsg = "{ \"msgtype\": \"text\", \"text\": {\"content\": \""+msg+"\"}}";
        StringEntity se = new StringEntity(textMsg, "utf-8");
        httppost.setEntity(se);

        HttpResponse response = httpclient.execute(httppost);
        if (response.getStatusLine().getStatusCode()== HttpStatus.SC_OK){
            String result= EntityUtils.toString(response.getEntity(), "utf-8");
            System.out.println(result);
        }
    }

    public static void sendErrorMsg(HashMap<String, String> msgMap){
        int scriptId = Integer.parseInt(msgMap.get("scriptId"));
        int totalGoods = Integer.parseInt(msgMap.get("totalGoods"));
        int successGoods = Integer.parseInt(msgMap.get("successGoods"));
        int failGoods = Integer.parseInt(msgMap.get("failGoods"));
        long startTime = Long.parseLong(msgMap.get("startTime"));
        long endTime = Long.parseLong(msgMap.get("endTime"));
        String storeName = msgMap.get("storeName");
        int useMinute = (int)Math.floor((endTime - startTime)/60.0);
        int useSecond = (int)Math.floor((endTime - startTime)%60.0);

        String msg = storeName+ msgZero+ scriptArray[scriptId] + msgOne+
                DateUtil.getDateString(new Date(startTime * 1000)) + " - " +
                DateUtil.getDateString(new Date(endTime * 1000)) +
                msgTwo + totalGoods + msgThree + successGoods+ msgFour+failGoods +
                msgFive + useMinute+ msgSix+ useSecond+msgSeven;
        if (scriptId == 3) {
            msg += msgEight+ msgMap.get("matchConditionNumber");
        }
        try {
            sendDingTalkMsg(msg);
        }catch (Exception e){
            e.printStackTrace();

        }
    }
}
