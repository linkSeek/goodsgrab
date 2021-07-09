package com.ssm.demo.utils;

public class ScriptStatusUtil {
    private static ScriptStatusUtil instance = null;
    private static boolean scriptIsRun = false;

    private ScriptStatusUtil() {
    }
    public static ScriptStatusUtil getInstance() {
        // 先判断实例是否存在，若不存在再对类对象进行加锁处理
        if (instance == null) {
            synchronized (ScriptStatusUtil.class) {
                if (instance == null) {
                    instance = new ScriptStatusUtil();
                }
            }
        }
        return instance;
    }

    public boolean checkScriptRun(){
        return scriptIsRun;
    }

    public void startScript(){
        scriptIsRun = true;
    }

    public void  closeScript() {
        scriptIsRun = false;
    }
}
