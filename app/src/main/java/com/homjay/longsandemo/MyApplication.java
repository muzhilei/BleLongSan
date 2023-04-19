package com.homjay.longsandemo;

import android.app.Application;

import com.homjay.longsandemo.utlis.CrashExceptionHandler;

/**
 * @author Homjay
 * @date 2023/3/24 16:24
 * @describe
 */
public class MyApplication extends Application {

    public static MyApplication app;
    private String tCmd = "";

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        CrashExceptionHandler crashExceptionHandler = CrashExceptionHandler.newInstance();
        crashExceptionHandler.init(getApplicationContext());
    }


    public static MyApplication getIns() {
        return app;
    }

    public String gettCmd() {
        return tCmd;
    }

    public void settCmd(String tCmd) {
        this.tCmd = tCmd;
    }
}
