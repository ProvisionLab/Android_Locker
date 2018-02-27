package com.dev.joks.lockscreen.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.dev.joks.lockscreen.Lockscreen;
import com.dev.joks.lockscreen.LockscreenUtil;
import com.dev.joks.lockscreen.R;
import com.dev.joks.lockscreen.SharedPrefsUtil;
import com.dev.joks.lockscreen.activity.PermissionActivity;
import com.dev.joks.lockscreen.event.ServiceStoppedEvent;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static com.dev.joks.lockscreen.service.StartLockService.LOCK_PASS;

/**
 * Created by Evgeniy on 24-Jan-18.
 */

public class LockScreenViewService extends Service {

    private static final String TAG = LockScreenViewService.class.getSimpleName();
    private static final String INTERFACE_NAME = "Android";
    private LayoutInflater mInflater = null;
    private View mLockscreenView = null;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private boolean mIsLockEnable = false;
    private boolean mIsSoftkeyEnable = false;
    private int mServiceStartId = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "LockScreenViewService started!");
        if (isLockScreenAble()) {
            if (null != mWindowManager) {
                if (null != mLockscreenView) {
                    mWindowManager.removeView(mLockscreenView);
                }
                mWindowManager = null;
                mParams = null;
                mInflater = null;
                mLockscreenView = null;
            }
            initState();
            initView();
            attachLockScreenView();
        }
        return LockScreenViewService.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "LockScreenViewService stopped!");
        detachLockScreenView();
    }


    private void initState() {

        mIsLockEnable = LockscreenUtil.getInstance(this).isStandardKeyguardState();
        if (mIsLockEnable) {
            mParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                        PixelFormat.TRANSLUCENT);
            } else {
                mParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
                        PixelFormat.TRANSLUCENT);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mIsLockEnable && mIsSoftkeyEnable) {
                mParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            } else {
                mParams.flags = WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
            }
        } else {
            mParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }

        if (null == mWindowManager) {
            mWindowManager = ((WindowManager) getSystemService(WINDOW_SERVICE));
        }
    }

    private void initView() {
        if (null == mInflater) {
            mInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        if (null == mLockscreenView) {
            mLockscreenView = mInflater.inflate(R.layout.view_lockscreen, null);

        }
    }

    private boolean isLockScreenAble() {
        return SharedPrefsUtil.getBooleanData(this, Lockscreen.ISLOCK);
    }


    private void attachLockScreenView() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent permissionActivityIntent = new Intent(this, PermissionActivity.class);
                permissionActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(permissionActivityIntent);

                LockscreenUtil.getInstance(this).getPermissionCheckSubject()
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                new Action1<Boolean>() {
                                    @Override
                                    public void call(Boolean aBoolean) {
                                        addLockScreenView();
                                    }
                                }
                        );
            } else {
                addLockScreenView();
            }
        } else {
            addLockScreenView();
        }

    }

    private void addLockScreenView() {
        if (null != mWindowManager && null != mLockscreenView && null != mParams) {
            mLockscreenView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
            mWindowManager.addView(mLockscreenView, mParams);

            WebView webView = (WebView) mLockscreenView.findViewById(R.id.webview);
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDisplayZoomControls(true);
            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            webView.addJavascriptInterface(new CustomJavascriptInterface(this), INTERFACE_NAME);
            webView.loadUrl("file:///" + Environment.getExternalStorageDirectory().toString() + File.separator + "LockScreen/lock_screen.html");
        }
    }

    private void detachLockScreenView() {
        if (null != mWindowManager && null != mLockscreenView && isAttachedToWindow()) {
            mWindowManager.removeView(mLockscreenView);
            mLockscreenView = null;
            mWindowManager = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isAttachedToWindow() {
        return mLockscreenView.isAttachedToWindow();
    }

    private class CustomJavascriptInterface {

        private Context context;

        CustomJavascriptInterface(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void receiveValue(String toast) {
            if (toast.equals(LOCK_PASS)) {
                UIUtil.hideKeyboard(LockScreenViewService.this, mLockscreenView);
                Toast.makeText(context, "Unlock", Toast.LENGTH_SHORT).show();
                Lockscreen.getInstance(LockScreenViewService.this).stopLockscreenService();
                detachLockScreenView();
                EventBus.getDefault().post(new ServiceStoppedEvent());
            } else {
                Toast.makeText(context, "Wrong password!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
