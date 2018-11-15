package com.platform.common;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.insplatform.core.utils.JsonUtil;
import com.insplatform.core.utils.TextUtil;
import com.platform.chat.LoginActivity;
import com.xiasuhuei321.loadingdialog.view.LoadingDialog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpRequest {

    //内网
//    private final String URL = "http://myhome.ticp.io:54413/insproject-web-app/app/";
//    private final String FILE_URL = "http://myhome.ticp.io:54413/insproject-file";

    private final String URL = Constant.BaseUrl + "/insproject-web-app/app/";
    private final String FILE_URL = Constant.BaseUrl + "/insproject-file";

    //服务器
//    private final String URL = "http://113.200.189.142:6018/insproject-web-app-grid/app/";
//    private final String FILE_URL = "http://113.200.189.142:6018/insproject-file";

    private int CONNECT_TIMEOUT = 60;
    private int READ_TIMEOUT = 100;
    private int WRITE_TIMEOUT = 60;

    public String getUrl() {
        return URL;
    }

    public String getFileUrl() {
        return FILE_URL;
    }

    /**
     * post请求
     *
     * @param context,传context,获取设备id需要
     * @param data,以map的形式传递参数
     * @param callBackSuccess,回调callback
     */
    public void post(final Context context, String url, Map<String, Object> data, final CallBackSuccess callBackSuccess) {
        if (BaseUtils.isNetworkConnected(context)) {
            post(context, url, data, callBackSuccess, false);
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "请先检查网络链接!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void post(final Context context, String url, Map<String, Object> data, final CallBackSuccess callBackSuccess, boolean showLoading) {
        if (showLoading) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    new LoadingDialog(context).show();
                }
            });
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build();
        if (data == null) {
            data = new HashMap<String, Object>();
        }
        data.put("appVersion", BaseUtils.getVersionName(context));
        data.put("deviceId", BaseUtils.getDeviceIMEI(context));
        if (TextUtil.isNotEmpty(BaseUtils.getSharedPreferences(context, "fu"))) {
            data.put("fu", BaseUtils.getSharedPreferences(context, "fu"));
        }
        FormBody.Builder formBody = new FormBody.Builder();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            formBody.add(BaseUtils.toString(entry.getKey()), BaseUtils.toString(entry.getValue()));
        }
        RequestBody requestBody = formBody.build();
        Request request = new Request.Builder().post(requestBody).url(URL + url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Exception", e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String back = response.body().string();
                Map<String, Object> data = JsonUtil.toObject(back, Map.class);
                if (Boolean.parseBoolean(BaseUtils.toString(data.get("success")))) {
                    /*
                     * 成功统一处理
                     * 返回成功,则是data
                     */
                    callBackSuccess.onCallBackSuccess(data.get("data"));
                } else {
                    /*
                     * 返回失败统一处理
                     * 只要失败,就有message
                     */
                    Log.w("-----*-*-*-", BaseUtils.toString(data));
                    if (data.get("result").equals("ACCOUNT_ERROR")) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "账号密码错误", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else if (data.get("result").equals("ACCOUNT_LOCKED")) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "账号已被锁定", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else if (data.get("result").equals("ACCOUNT_SUCCESS")) {
                        callBackSuccess.onCallBackSuccess(data);
                    } else if (data.get("message").equals("TIMEOUT_OTHER_LOGIN")) {
                        if (context instanceof Activity) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    DialogUtils.showAlertDialog((Activity) context, "该账号在别处登录,请重新登录", "去登录", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            BaseUtils.deleteSharedPreferences(context);
                                            Intent intent = new Intent(context, LoginActivity.class);
                                            context.startActivity(intent);
                                            AppManager appManager = AppManager.getInstance();
                                            appManager.killAllActivity();
                                            ((Activity) context).overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                        }
                                    });
                                }
                            });
                        } else {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "该账号在别处登录,请重新登录", Toast.LENGTH_SHORT).show();
                                    BaseUtils.deleteSharedPreferences(context);
                                    Intent intent = new Intent(context, LoginActivity.class);
                                    context.startActivity(intent);
                                    AppManager appManager = AppManager.getInstance();
                                    appManager.killAllActivity();
                                    ((Activity) context).overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                }
                            });
                        }
                    } else if (data.get("message").equals("VERSION_ERROR")) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                DialogUtils.showAlertDialog((Activity) context, "携带版本号丢失,请联系客服!");
                            }
                        });
                    } else if (data.get("message").toString().indexOf("latestVersion") > -1) {
                        final Map<String, Object> map = (Map<String, Object>) ((Map<String, Object>) data.get("message")).get("latestVersion");
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                new UpdateAppService((Activity) context).checkUpdate(BaseUtils.toString(map.get("version")), BaseUtils.toString(map.get("describe")), new HttpRequest().getFileUrl() + BaseUtils.toString(map.get("filePath")), "xxxq_grid.apk");
                            }
                        });
                    } else {
                        Log.w("*************", data.get("message").toString());
                    }
                }
            }
        });

        if (showLoading) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    new LoadingDialog(context).close();
                }
            });
        }
    }
}
