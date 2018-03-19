package com.dev.joks.lockscreen.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.dev.joks.lockscreen.R;
import com.dev.joks.lockscreen.SharedPrefsUtil;
import com.dev.joks.lockscreen.activity.LockNoPasscode;
import com.dev.joks.lockscreen.activity.PasswordActivity;
import com.dev.joks.lockscreen.event.ServiceStoppedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import static com.dev.joks.lockscreen.activity.MainActivity.HOURS;
import static com.dev.joks.lockscreen.activity.MainActivity.ISLOCK;
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

    private static final int NOTIFICATION_ID = 99;

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

        runAsForeground();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SharedPrefsUtil.putBooleanData(StartLockService.this, ISLOCK, true);
                startLock();
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
                Log.d(TAG, "Message get! Timer started");
                startLock();
            }
        }, (hours * 3600 + minutes * 60 + seconds) * TO_MILLISECONDS);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "Task removed");
        Intent intent = new Intent(this, StartLockService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 30 * 1000, pintent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        EventBus.getDefault().unregister(this);
        Log.d(TAG, "Stop Main Service");

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(1);
    }

    private void runAsForeground() {
        Intent notificationIntent = new Intent(this, PasswordActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(getString(R.string.app_name) + " service is running")
                .setContentIntent(pendingIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void startLock() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                final Intent intent = new Intent(StartLockService.this, LockNoPasscode.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Log.d(TAG, "You need overlay permission!");
            }
        } else {
            Intent intent = new Intent(StartLockService.this, LockNoPasscode.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}
