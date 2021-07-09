package com.ssm.demo.utils;
import com.ssm.demo.controller.AdminUserControler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * @author yunfeng
 * @version V1.0
 * @date 2018/5/3 16:33
 */
public class HttpClientUtil {

    final static Logger logger = Logger.getLogger(HttpClientUtil.class);

    /**
     * @param url
     * @return
     */
    public String getData(String url) {
        return getData(url, "", 0);
    }

    public String getData(String url, String proxyUrl, int port) {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);
        if(proxyUrl.length() > 0) {
            client.getHostConfiguration().setProxy(proxyUrl,port);
        }
//        System.out.println("reqUrl: " + url);
        String respStr = "";
        try {
            int statusCode = client.executeMethod(method);
            respStr = method.getResponseBodyAsString();
            method.releaseConnection();
        } catch (IOException e) {
            logger.error("发送HTTP GET请求失败！详情：" + e.toString());
            e.printStackTrace();
        }
        return respStr;
    }

    /**
     * @param url
     * @param jsonStr
     * @return
     */
    public static String postJson(String url, String jsonStr) {
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(url);
        method.setRequestHeader("Content-Type", "application/json;charset=utf-8");
        String postBody = buildPostData(jsonStr);
//        log.info("reqUrl: " + url);
//        log.info("postBody:\r\n" + postBody);
        method.setRequestBody(postBody);
        String respStr = "";
        try {
            int statusCode = client.executeMethod(method);
//            log.info("Status Code = " + statusCode);
            respStr = method.getResponseBodyAsString();
//            log.info("respStr:" + respStr);
            method.releaseConnection();
        } catch (IOException e) {
//            log.error("发送HTTP POST JSON请求失败！详情：" + e.toString());
            e.printStackTrace();
        }
        return respStr;
    }

    /**
     * @param url
     * @param jsonStr
     * @return
     */
    public static String postJsonHeader(String url, Map<String, String> headerMap, String jsonStr) {
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(url);
        method.setRequestHeader("Content-Type", "application/json;charset=utf-8");
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            method.setRequestHeader(key, val);
        }
        String postBody = buildPostData(jsonStr);
//        log.info("HTTP　ReqUrl: " + url);
//        log.info("HTTP　ReqContent: Content-Type: application/json;charset=utf-8");
//        log.info("HTTP PostBody:\r\n" + postBody);
        method.setRequestBody(postBody);
        String respStr = "";
        try {
            int statusCode = client.executeMethod(method);
            respStr = method.getResponseBodyAsString();
            if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
                Header header = method.getResponseHeader("Location");
                String location = "";
                if (header != null) {
                    location = header.getValue();
                    method.setURI(new URI(location));
                    statusCode = client.executeMethod(method);
                    respStr = method.getResponseBodyAsString();
                }

            }
//            log.info("Status Code = " + statusCode);
//            log.info("respStr:" + respStr);
            method.releaseConnection();
        } catch (IOException e) {
//            String detail = "发送HTTP POST JSON请求失败！详情：" + e.toString();
//            log.error(detail, e);
            e.printStackTrace();
            return null;
        }
        return respStr;
    }


    /**
     * @param url     url
     * @param formStr form参数
     * @return 请求返回
     */
    public static String postForm(String url, String formStr) {
//        log.info("post URL：" + url);
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(url);
        method.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        String postBody = buildPostData(formStr);
//        log.info("postBody:\n" + postBody);
        method.setRequestBody(postBody);
        int statusCode = 0;
        String respStr = "";
        try {
            statusCode = client.executeMethod(method);
//            log.info("http return status code = " + statusCode);
            respStr = method.getResponseBodyAsString();
//            log.info("http return respStr = " + respStr);
        } catch (IOException e) {
//            log.error("发送http form请求失败！详情：" + e.toString());
            e.printStackTrace();
        }
        method.releaseConnection();
        return respStr;
    }


    /**
     * @param formStr form参数
     * @return
     */
    private static String buildPostData(String formStr) {
        StringBuilder sb = new StringBuilder();
        sb.append(formStr);
        sb.append("\r\n");
        sb.append("\r\n");
        return sb.toString();
    }
}