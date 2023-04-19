package com.homjay.longsandemo.utlis;

import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @Author: taimin
 * @Date: 2020/3/17
 * @Description: 定时任务
 */

public class TimerTasker {
    private Timer timer;
    private TimerTask timerTask;
    private OnTimerRun onTimerRun;

    private long delayTime = 0;
    private long periodTime;

    public TimerTasker(long delayTime, long periodTime, OnTimerRun onTimerRun) {
        this.delayTime = delayTime;
        this.periodTime = periodTime;
        this.onTimerRun = onTimerRun;
    }

    public TimerTasker(long periodTime, OnTimerRun onTimerRun) {
        this.periodTime = periodTime;
        this.onTimerRun = onTimerRun;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                if (onTimerRun != null) {
                    onTimerRun.onRun();
                }
            }
        }
    };

    public void startTimer() {
        cancelTimer();
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        };
        timer.schedule(timerTask, delayTime, periodTime);
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    public interface OnTimerRun {
        void onRun();
    }
}
