package com.dev.joks.lockscreen;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * Created by Evgeniy on 23-Jan-18.
 */

public class CustomJavascriptInterface {

    private static String CORRECT_PASS = "11";

    private Context context;

    public CustomJavascriptInterface(Context context) {
        this.context = context;
    }

    @JavascriptInterface
    public void receiveValue(String toast) {
        if (toast.equals(CORRECT_PASS)) {
            Toast.makeText(context, "Unlock", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Wrong password!", Toast.LENGTH_SHORT).show();
        }
    }
}
