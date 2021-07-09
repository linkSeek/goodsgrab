package com.ssm.demo.utils;

import com.google.common.base.Function;
import com.ssm.demo.entity.Store;
import com.ssm.demo.service.impl.AdminUserServiceImpl;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author aaron
 */
public class SeleniumUtil {

    final static Logger logger = Logger.getLogger(SeleniumUtil.class);
    public void noonLogin(WebDriver driver, Store store)  {
        // 打开登录页面
        driver.get("https://login.noon.partners/en/?page=core");
        WebDriver.Window window = driver.manage().window();
        window.maximize();
        logger.info("start login  -----------------");

        // 判断页面元素是否存在
        WebDriverWait wait = new WebDriverWait(driver,20);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));

        // 获取登录框元素
        WebElement emailElem = driver.findElement(By.name("email"));
        emailElem.sendKeys(store.getEmail());

        // 输入密码
        WebElement passwordElem = driver.findElement(By.name("password"));
        passwordElem.sendKeys(store.getPassword());

        // 点击登录
        driver.findElement(By.xpath("/html/body/div[1]/div/div/div[2]/div/div[1]/div[3]/div/button")).click();
        // dashboard显示出来完成之后进行下一步
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[3]/div/div/div[1]/h1")));
    }

    public double getMyStorePrice (WebDriver driver, Actions actions, final String goodsId) throws Exception {

        // 判断页面元素是否存在
        WebDriverWait wait = new WebDriverWait(driver,20);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[3]/div/div/div[1]/h1")));


        // 获取搜索框内容
        WebElement searchElem = driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[3]/div/div/div[1]/div[1]/div/div[1]/input"));
        actions.doubleClick(searchElem).perform();
        searchElem.sendKeys(goodsId);

        // 点击搜索按钮
        driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[3]/div/div/div[1]/div[1]/div/div[1]/div/i")).click();

        //判断是否有搜索结果
        wait.until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver driver) {
                // 海外环境 //*[@id="__next"]/div/div[2]/div[3]/div/div/div[2]/div/div/div[3]/div/table/tbody/tr/td[5]
                // 迪拜当地环境 //*[@id="__next"]/div/div[2]/div[3]/div/div/div[2]/div/div/div[3]/div/table/tbody/tr/td[4]
                if(driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[3]/div/div/div[2]/div/div/div[3]/div/table/tbody/tr/td[4]")).getText().equals(goodsId)) {
                    return driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[3]/div/div/div[2]/div/div/div[3]/div/table/tbody/tr/td[4]"));
                }
                return null;
            }
        });
        // 获取价格
        // 海外环境 //*[@id="__next"]/div/div[2]/div[3]/div/div/div[2]/div/div/div[3]/div/table/tbody/tr/td[8]/div/div[2]/span[2]
        // 当地环境 //*[@id="__next"]/div/div[2]/div[3]/div/div/div[2]/div/div/div[3]/div/table/tbody/tr[1]/td[7]/div/div[2]/span[2]
        String price = driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[3]/div/div/div[2]/div/div/div[3]/div/table/tbody/tr[1]/td[7]/div/div[2]/span[2]")).getText().replace("AED ", "");
//        String dealPrice = driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[3]/div/div/div[2]/div/div/div[3]/div/table/tbody/tr/td[7]/div/div[3]/span[2]")).getText().replace("AED ", "");
//        if ("--".equals(dealPrice)) {
//            return Double.parseDouble(price);
//        } else {
//            double dealPriceNumber = Double.parseDouble(dealPrice);
//            double priceNumber = Double.parseDouble(price);
//            return Math.min(priceNumber, dealPriceNumber);
//        }
        return Double.parseDouble(price);
    }

    public void changeMyStorePrice(WebDriver driver, Actions actions, double newPrice) throws Exception {
        double trimPrice = (double) Math.round(newPrice * 100) / 100;
        // 点击详情按钮
        driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[3]/div/div/div[2]/div/div/div[3]/div/table/tbody/tr/td[10]/div/div[2]/div/span/div/i")).click();

        WebDriverWait wait = new WebDriverWait(driver,20);

        // 判断详情弹窗是否出现
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[3]/div/div/div[5]/div[1]/div[2]/div/div/div[2]/div[1]/div[3]/div[2]/div/div/input")));
        WebElement priceElem = driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[3]/div/div/div[5]/div[1]/div[2]/div/div/div[2]/div[1]/div[3]/div[2]/div/div/input"));

        // 双击选中需改变的文字，传递新价格
        actions.doubleClick(priceElem).perform();
        priceElem.sendKeys(String.valueOf(trimPrice));

        // 点击确定
        driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[3]/div/div/div[5]/div[1]/div[3]/div[3]/div")).click();

    }

    // 清理搜索框
    public void clearSearchText(WebDriver driver, Actions actions) {
        WebElement searchElem = driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[3]/div/div/div[1]/div[1]/div/div[1]/input"));
        actions.doubleClick(searchElem).perform();
        searchElem.sendKeys(Keys.BACK_SPACE);
    }

    public void pageReload(WebDriver driver) {
        driver.get("https://catalog.noon.partners/en-ae/catalog?limits=20&page=1");
        WebDriverWait wait = new WebDriverWait(driver,20);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[3]/div/div/div[2]/div/div/div[3]/div/table/tbody/tr[1]/td[3]")));
    }

}
