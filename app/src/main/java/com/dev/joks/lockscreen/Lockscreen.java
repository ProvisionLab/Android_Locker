package com.dev.joks.lockscreen;

import android.content.Context;
import android.content.Intent;

import com.dev.joks.lockscreen.service.LockScreenService;
import com.dev.joks.lockscreen.service.LockScreenViewService;

/**
 * Created by Evgeniy on 24-Jan-18.
 */

public class Lockscreen {
    private Context mContext = null;
    public static final String ISSOFTKEY = "ISSOFTKEY";
    public static final String ISLOCK = "ISLOCK";
    private static Lockscreen mLockscreenInstance;

    public static Lockscreen getInstance(Context context) {
        if (mLockscreenInstance == null) {
            if (null != context) {
                mLockscreenInstance = new Lockscreen(context);
            } else {
                mLockscreenInstance = new Lockscreen();
            }
        }
        return mLockscreenInstance;
    }

    private Lockscreen() {
        mContext = null;
    }

    private Lockscreen(Context context) {
        mContext = context;
    }

    public void startLockscreenService() {
        Intent startLockscreenIntent = new Intent(mContext, LockScreenService.class);
//        startLockscreenIntent.putExtra(LockscreenService.LOCKSCREENSERVICE_FIRST_START, true);
        mContext.startService(startLockscreenIntent);

    }

    public void stopLockscreenService() {
        Intent stopLockscreenViewIntent = new Intent(mContext, LockScreenViewService.class);
        mContext.stopService(stopLockscreenViewIntent);
        Intent stopLockscreenIntent = new Intent(mContext, LockScreenService.class);
        mContext.stopService(stopLockscreenIntent);
    }
}
