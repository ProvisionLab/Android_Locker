package com.dev.joks.lockscreen.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.widget.Toast;

import com.dev.joks.lockscreen.R;
import com.dev.joks.lockscreen.SharedPrefsUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.dev.joks.lockscreen.activity.MainActivity.PASSWORD;

public class PasswordActivity extends AppCompatActivity {

    @BindView(R.id.password)
    AppCompatEditText enterPassEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        ButterKnife.bind(this);

        if (SharedPrefsUtil.getStringData(this, PASSWORD).isEmpty()) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @OnClick(R.id.enter)
    public void onClick() {
        String currentPass = SharedPrefsUtil.getStringData(this, PASSWORD);
        String enteredPass = enterPassEditText.getText().toString();

        if (enteredPass.isEmpty() || !currentPass.equals(enteredPass)) {
            Toast.makeText(this, "Password incorrect!", Toast.LENGTH_SHORT).show();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}
