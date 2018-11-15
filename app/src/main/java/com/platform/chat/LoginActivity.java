package com.platform.chat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.insplatform.core.utils.TextUtil;
import com.platform.common.AppManager;
import com.platform.common.BaseActivity;
import com.platform.common.BaseUtils;
import com.platform.common.CallBackSuccess;
import com.platform.common.HttpRequest;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends BaseActivity {

    private final String TAG = "LoginActivity";
    private TextView account, password;
    private Button login;
    private String fu;
    private long mExitTime;//App退出时间记录

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        BaseUtils.initHeaderBar(this);
        fu = BaseUtils.getSharedPreferences(this, "fu");
        init();
        initData();
        if (TextUtil.isNotEmpty(fu)) {
            Intent intent = new Intent(LoginActivity.this, UserListActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void init() {
        //检测权限
        checkPermissions();
        account = $(R.id.activity_login_account);
        password = $(R.id.activity_login_password);
        login = $(R.id.activity_login_login);
        login.setOnClickListener(l);
    }

    View.OnClickListener l = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.activity_login_login:
                    login();
                    break;
            }
        }
    };

    private void login() {
        String aa = account.getText().toString();
        String pp = password.getText().toString();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userName", aa);
        map.put("password", pp);
        new HttpRequest().post(this, "login/enter", map, new CallBackSuccess() {
            @Override
            public void onCallBackSuccess(Object data) {
                if ("ACCOUNT_SUCCESS".equals(((Map<String, Object>) data).get("result"))) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            showToast("登录成功");
                        }
                    });
                    BaseUtils.putSharedPreferences(LoginActivity.this, "fu", ((Map<String, Object>) data).get("fu").toString());
                    Intent intent = new Intent(LoginActivity.this, UserListActivity.class);
                    startActivity(intent);
                } else if ("ACCOUNT_LOCKED".equals(((Map<String, Object>) data).get("result"))) {
                    showToast("账号状态异常！");
                } else if ("ACCOUNT_ERROR".equals(((Map<String, Object>) data).get("result"))) {
                    showToast("账号或密码错误！！");
                }
            }
        }, true);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initTitle() {

    }

    //检测权限
    private void checkPermissions() {
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
        };
        int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                BaseUtils.showToast("再按一次退出应用", context);
                mExitTime = System.currentTimeMillis();
            } else {
//                DBManager.getInstance().closeDB();
                AppManager.getInstance().AppExit(context);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

