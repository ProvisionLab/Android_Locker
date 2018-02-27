package com.dev.joks.lockscreen.activity;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.dev.joks.lockscreen.AdminReceiver;
import com.dev.joks.lockscreen.R;
import com.dev.joks.lockscreen.SharedPrefsUtil;
import com.dev.joks.lockscreen.service.StartLockService;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.dev.joks.lockscreen.Lockscreen.ISLOCK;

public class MainActivity extends AppCompatActivity {

    public static final String HOURS = "hours";
    public static final String MINUTES = "minutes";
    public static final String SECONDS = "seconds";
    public static final int RESULT_ENABLE = 1111;
    public static final String PASSWORD = "pass";
    private static final String TAG = MainActivity.class.getSimpleName();
    private DevicePolicyManager policyManager;
    private ComponentName componentName;

    @BindView(R.id.switch_lock)
    SwitchCompat mSwitchCompat;

    @BindView(R.id.hours_spinner)
    Spinner hoursSpinner;

    @BindView(R.id.minutes_spinner)
    Spinner minutesSpinner;

    @BindView(R.id.seconds_spinner)
    Spinner secondsSpinner;

    @BindView(R.id.old_pass)
    EditText oldPasswordEditText;

    @BindView(R.id.new_pass)
    EditText newPasswordEditText;

    @BindView(R.id.new_pass_repeat)
    EditText newPasswordRepeatEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
//        getWindow().addFlags(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        createFolder();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "You need this permission for creating empty folder", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .check();

        policyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(this, AdminReceiver.class);

        initUI();

        if (!policyManager.isAdminActive(componentName)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "We need admin permission for this app to prevent application from being uninstalled");
            startActivityForResult(intent, RESULT_ENABLE);
        }

        mSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    File folder = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "LockScreen/");
                    boolean isEmptyDir = isDirEmpty(folder);
                    String pass = SharedPrefsUtil.getStringData(MainActivity.this, PASSWORD);

                    if (isEmptyDir) {
                        Toast.makeText(MainActivity.this, "Please, put html file into 'ScreenLock' folder!", Toast.LENGTH_SHORT).show();
                        mSwitchCompat.setChecked(false);
                    } else if (pass.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Password not set!", Toast.LENGTH_SHORT).show();
                        mSwitchCompat.setChecked(false);
                    } else {
                        mSwitchCompat.setChecked(true);
                    }
                }
            }
        });
    }

    private boolean isDirEmpty(final File file) {
        if (file.isDirectory()) {
            if (file.list().length > 0) {
                Log.d(TAG, "Directory is not empty");
                return false;
            } else {
                Log.d(TAG, "Directory is empty");
                return true;
            }
        } else {
            return true;
        }
    }

    private void initUI() {

        if (!SharedPrefsUtil.getStringData(this, PASSWORD).isEmpty()) {
            oldPasswordEditText.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "Lock " + SharedPrefsUtil.getBooleanData(this, ISLOCK));

        if (SharedPrefsUtil.getBooleanData(this, ISLOCK)) {
            mSwitchCompat.setChecked(true);
        } else {
            mSwitchCompat.setChecked(false);
        }

        String[] hoursArray = new String[24];
        for (int i = 0; i <= 23; i++) {
            hoursArray[i] = String.valueOf(i);
        }

        String[] minutesArray = new String[60];
        String[] secondsArray = new String[59];
        for (int i = 0; i <= 59; i++) {
            minutesArray[i] = String.valueOf(i);
        }

        for (int i = 0; i <= 58; i++) {
            secondsArray[i] = String.valueOf(i + 1);
        }

        ArrayAdapter<String> hoursSpinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        hoursArray);
        hoursSpinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        hoursSpinner.setAdapter(hoursSpinnerArrayAdapter);

        ArrayAdapter<String> minutesSpinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        minutesArray);
        minutesSpinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        minutesSpinner.setAdapter(minutesSpinnerArrayAdapter);

        ArrayAdapter<String> secondsSpinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        secondsArray);
        secondsSpinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        secondsSpinner.setAdapter(secondsSpinnerArrayAdapter);


        int hours = SharedPrefsUtil.getIntData(MainActivity.this, HOURS);
        int minutes = SharedPrefsUtil.getIntData(MainActivity.this, MINUTES);
        int seconds = SharedPrefsUtil.getIntData(MainActivity.this, SECONDS);

        Log.d(TAG, "Time " + hours + " " + minutes + " " + seconds);

        hoursSpinner.setSelection(hours == 0 ? hours : hours - 1);
        minutesSpinner.setSelection(minutes == 0 ? minutes : minutes - 1);
        secondsSpinner.setSelection(seconds == 0 ? seconds : seconds - 1);
    }

    private void createFolder() {
        String lockScreenFolder = "/LockScreen/";
        File f = new File(Environment.getExternalStorageDirectory(), lockScreenFolder);
        if (!f.exists()) {
            f.mkdir();
            Log.d(TAG, "Create folder");
        }
    }

    @OnClick({R.id.save_changes_btn, R.id.close_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save_changes_btn:
                String oldPass = oldPasswordEditText.getText().toString();
                String newPass = newPasswordEditText.getText().toString();
                String newPassRepeat = newPasswordRepeatEditText.getText().toString();

                if (newPass.isEmpty()) {
                    Toast.makeText(this, "Please enter the password!", Toast.LENGTH_SHORT).show();
                } else {
                    if (oldPass.equals(SharedPrefsUtil.getStringData(this, PASSWORD)) && newPass.equals(newPassRepeat)) {
                        SharedPrefsUtil.putStringData(this, PASSWORD, newPass);
                        Toast.makeText(this, "Password saved!", Toast.LENGTH_SHORT).show();
                        oldPasswordEditText.setVisibility(View.VISIBLE);
                        clearFields();
                    } else {
                        Toast.makeText(this, "Please enter correct password!", Toast.LENGTH_SHORT).show();
                        clearFields();
                    }
                }
                break;
            case R.id.close_btn:
                if (mSwitchCompat.isChecked()) {

                    int hours = Integer.parseInt(hoursSpinner.getSelectedItem().toString());
                    int minutes = Integer.parseInt(minutesSpinner.getSelectedItem().toString());
                    int seconds = Integer.parseInt(secondsSpinner.getSelectedItem().toString());

                    SharedPrefsUtil.putIntData(MainActivity.this, HOURS, hours);
                    SharedPrefsUtil.putIntData(MainActivity.this, MINUTES, minutes);
                    SharedPrefsUtil.putIntData(MainActivity.this, SECONDS, seconds);

                    Intent intent = new Intent(MainActivity.this, StartLockService.class);
                    startService(intent);
                    SharedPrefsUtil.putBooleanData(MainActivity.this, ISLOCK, true);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this)
                            .setContentTitle(getString(R.string.app_name) + " service is running")
                            .setSmallIcon(R.mipmap.ic_launcher_round);

                    Notification notification = builder.build();

                    notification.flags |= Notification.FLAG_NO_CLEAR
                            | Notification.FLAG_ONGOING_EVENT;
                    NotificationManager notifier = (NotificationManager)
                            getSystemService(Context.NOTIFICATION_SERVICE);
                    notifier.notify(1, notification);
                } else {
                    SharedPrefsUtil.putBooleanData(MainActivity.this, ISLOCK, false);
                    stopService(new Intent(MainActivity.this, StartLockService.class));
                }
                finish();
                break;
        }
    }

    private void clearFields() {
        oldPasswordEditText.setText("");
        newPasswordEditText.setText("");
        newPasswordRepeatEditText.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case RESULT_ENABLE:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Admin features enabled", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Problem with enabling admin features", Toast.LENGTH_LONG).show();
                }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}