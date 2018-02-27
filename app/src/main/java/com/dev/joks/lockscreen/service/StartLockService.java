package com.dev.joks.lockscreen.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.dev.joks.lockscreen.Lockscreen;
import com.dev.joks.lockscreen.LockscreenUtil;
import com.dev.joks.lockscreen.SharedPrefsUtil;
import com.dev.joks.lockscreen.event.ServiceStoppedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Timer;
import java.util.TimerTask;

import static com.dev.joks.lockscreen.Lockscreen.ISLOCK;
import static com.dev.joks.lockscreen.activity.MainActivity.HOURS;
import static com.dev.joks.lockscreen.activity.MainActivity.MINUTES;
import static com.dev.joks.lockscreen.activity.MainActivity.SECONDS;

public class StartLockService extends Service {

    private static final String TAG = StartLockService.class.getSimpleName();
    private Timer timer;
    private static final int TO_MILLISECONDS = 1000;
    public static final String LOCK_PASS = "11";
    private int hours;
    private int minutes;
    private int seconds;

    public StartLockService() {
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Start Main Service");
        timer = new Timer();
        hours = SharedPrefsUtil.getIntData(this, HOURS);
        minutes = SharedPrefsUtil.getIntData(this, MINUTES);
        seconds = SharedPrefsUtil.getIntData(this, SECONDS);

        Log.d(TAG, "Time " + hours + " " + minutes + " " + seconds + " " + (hours + minutes + seconds) * TO_MILLISECONDS);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SharedPrefsUtil.putBooleanData(StartLockService.this, ISLOCK, true);

                if (!LockscreenUtil.isServiceRunning(LockScreenService.class, StartLockService.this)) {
                    Lockscreen.getInstance(StartLockService.this).startLockscreenService();
                }
                Log.d(TAG, "Timer started");
            }
        }, (hours * 3600 + minutes * 60 + seconds) * TO_MILLISECONDS);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ServiceStoppedEvent event) {

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SharedPrefsUtil.putBooleanData(StartLockService.this, ISLOCK, true);

                if (!LockscreenUtil.isServiceRunning(LockScreenService.class, StartLockService.this)) {
                    Lockscreen.getInstance(StartLockService.this).startLockscreenService();
                }
                Log.d(TAG, "Message get! Timer started");
            }
        }, (hours * 3600 + minutes * 60 + seconds) * TO_MILLISECONDS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        EventBus.getDefault().unregister(this);
        Log.d(TAG, "Stop Main Service");

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(1);
    }
}
