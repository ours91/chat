package com.platform.application;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.insplatform.core.utils.JsonUtil;
import com.insplatform.core.utils.TextUtil;
import com.platform.common.BaseUtils;
import com.platform.common.Constant;
import com.platform.common.MyLog;
import com.platform.common.Utils;
import com.platform.service.CallBack;
import com.platform.service.SocketIoService;
import com.xiasuhuei321.loadingdialog.manager.StyleManager;
import com.xiasuhuei321.loadingdialog.view.LoadingDialog;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.platform.common.Constant.auth_Token;

/**
 * 极光推送   一般建议在自定义 Application 类里初始化。也可以在主 Activity 里。
 */
public class BaseApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "CHAT_PROJECT";
    private static Socket socket;
    private boolean isBackGround = true;//是否在前台
    private static Handler handler;
    private static BaseApplication application;

    public static final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            MyLog.i("ChatApplication ===>", "打开连接");
            socket.open();
            handler.postDelayed(runnable, 6000);
        }
    };

    public static void Run() {
        handler.postDelayed(runnable, 6000);
    }

    public static Socket getSocket() {
        if (socket == null) {
            IO.Options opts = new IO.Options();
            opts.forceNew = false;
            opts.reconnection = true;
            opts.reconnectionDelay = 2000;      //延迟
            opts.reconnectionDelayMax = 6000;
            opts.reconnectionAttempts = -1;
            opts.timeout = 16000;
            opts.query = "auth_token=" + auth_Token;
            try {
                socket = IO.socket(Constant.BaseUrl + ":3000", opts);
                return socket;
            } catch (Exception e) {
            }
        }
        return socket;
    }

    public static void closeSocket() {
        socket = null;
    }

    @Override
    public void onCreate() {
        //初始化方法写这
        super.onCreate();

        //圆形头像初始化
        Fresco.initialize(this);

        Utils.init(getApplicationContext());
        handler = new Handler();
        application = this;
        //极光推送初始化
//        JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
//        JPushInterface.init(this);     		// 初始化 JPush

        //初始化loading
        StyleManager s = new StyleManager();
        //在这里调用方法设置s的属性
        //code here...
        s.Anim(false).repeatTime(0).contentSize(-1).intercept(true);
        LoadingDialog.initStyle(s);
    }

    public static Handler getHandler() {
        return handler;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            isBackGround = true;
            MyLog.i("----", "APP遁入后台");
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (isBackGround) {
            isBackGround = false;
            MyLog.i("bo", "APP回到了前台");
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

}
