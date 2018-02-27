package com.dev.joks.lockscreen.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.dev.joks.lockscreen.CustomJavascriptInterface;
import com.dev.joks.lockscreen.Lockscreen;
import com.dev.joks.lockscreen.LockscreenUtil;
import com.dev.joks.lockscreen.R;
import com.dev.joks.lockscreen.SharedPrefsUtil;
import com.dev.joks.lockscreen.service.LockScreenViewService;

public class LockActivity extends AppCompatActivity {

    private static final String TAG = LockActivity.class.getSimpleName();
    private static final String INTERFACE_NAME = "Android";
    private static final String CORRECT_PASS = "1111";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        getWindow().setType(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        WebView webView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDisplayZoomControls(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.addJavascriptInterface(new CustomJavascriptInterface(this), INTERFACE_NAME);
        webView.loadUrl("file:///android_asset/lock_screen.html");

        mMainHandler = new SendMassgeHandler();

        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        manager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        setLockGuard();
    }
//
//    @Override
//    public void onBackPressed() {
//    }

    private RelativeLayout mLockscreenMainLayout = null;

    public static SendMassgeHandler mMainHandler = null;

    public PhoneStateListener phoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {

            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    break;
                default:
                    break;
            }
        }

    };

    private class SendMassgeHandler extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            finish();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    private void setLockGuard() {
        boolean isLockEnable = false;
        isLockEnable = LockscreenUtil.getInstance(this).isStandardKeyguardState();

        Intent startLockscreenIntent = new Intent(this, LockScreenViewService.class);
        startService(startLockscreenIntent);

        boolean isSoftkeyEnable = LockscreenUtil.getInstance(this).isSoftKeyAvail(this);
        SharedPrefsUtil.putBooleanData(this, Lockscreen.ISSOFTKEY, isSoftkeyEnable);
        if (!isSoftkeyEnable) {
            mMainHandler.sendEmptyMessage(0);
        } else if (isSoftkeyEnable) {
            if (isLockEnable) {
                mMainHandler.sendEmptyMessage(0);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
    }
}
