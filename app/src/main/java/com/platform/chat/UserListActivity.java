package com.platform.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.insplatform.core.utils.JsonUtil;
import com.insplatform.core.utils.TextUtil;
import com.platform.adapter.UserListAdapter;
import com.platform.application.BaseApplication;
import com.platform.common.AppManager;
import com.platform.common.BaseActivity;
import com.platform.common.BaseRecycleAdapter;
import com.platform.common.BaseUtils;
import com.platform.common.CallBackSuccess;
import com.platform.common.HeaderBar;
import com.platform.common.HttpRequest;
import com.platform.common.MyLog;
import com.platform.common.ScreenListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class UserListActivity extends BaseActivity {
    private final String TAG = "UserListActivity";
    private Socket socket;
    private HeaderBar headerBar;
    private List<Map<String, Object>> userList;
    private UserListAdapter userListAdapter;
    private RecyclerView recyclerView;
    ScreenListener screenListener;//锁屏监听
    boolean onScreenOff = false;
    private Map<String, Object> fu;
    private long mExitTime;//app退出时间记录

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        initTitle();
        init();
        initScreenListener();
        initSocketData();

    }

    private void initSocketData() {
        BaseApplication.closeSocket();
        BaseApplication app = (BaseApplication) getApplication();
        socket = app.getSocket();
        socket.open();
        socket.on("connect", onConnect)
                .on(Socket.EVENT_DISCONNECT, onDisconnect)
                .on(Socket.EVENT_CONNECT_ERROR, onConnectError)
                .on(Socket.EVENT_CONNECT_TIMEOUT, onConnectTimeoutError)
                .on("user-state-change", getUserList);
        socket.connect();
    }

    @Override
    protected void init() {
        //用户信息对象
        fu = JsonUtil.toObject(BaseUtils.getSharedPreferences(context, "fu"), Map.class);
        userList = new ArrayList<Map<String, Object>>();
        recyclerView = (RecyclerView) findViewById(R.id.activity_main2_user_list_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (screenListener != null) {
            screenListener.unregisterListener();
        }
        socket.disconnect();
        socket.off(Socket.EVENT_CONNECT, onConnect);// 连接成功
        socket.off(Socket.EVENT_DISCONNECT, onDisconnect);// 断开连接
        socket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);// 连接异常
        socket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectTimeoutError);// 连接超时
        socket.off("user-state-change");
        socket = null;
    }

    @Override
    protected void initData() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", fu.get("id"));
        map.put("limit", 999);
        new HttpRequest().post(context, "chatuser/loadList", map, new CallBackSuccess() {
            @Override
            public void onCallBackSuccess(Object data) {
                userList = (List<Map<String, Object>>) data;
                if (socket.connected()) {
                    socket.emit("get-user-list", "");
                } else {
                    MyLog.e("-----", "socket已掉线");
                }
            }
        });
    }

    /**
     * 消息处理,通知主线程更新ui
     */
    private Handler handler = new Handler() {
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    setListValues();
                    break;
            }
        }
    };

    private Handler handler1 = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    setListValues();
                    break;
            }
            return false;
        }
    });


    private void setListValues() {
        userListAdapter = new UserListAdapter(this, R.layout.adapter_user_list, userList);
        recyclerView.setAdapter(userListAdapter);
        userListAdapter.setOnItemClickListner(new BaseRecycleAdapter.OnItemClickListner() {
            @Override
            public void onItemClickListner(View v, int position) {
                if (socket.connected()) {
                    Intent intent = new Intent(UserListActivity.this, ChatDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("name", BaseUtils.toString(userList.get(position).get("nickName")));
                    bundle.putString("id", BaseUtils.toString(userList.get(position).get("id")));
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    BaseUtils.showToast("Socket链接已断开", context);
                }
            }
        });
        userListAdapter.setOnItemLongClickListner(new BaseRecycleAdapter.OnItemLongClickListner() {
            @Override
            public void onItemLongClickListner(View v, int position) {
                return;
            }
        });
    }

    private void connectionSocket() {
        if (socket.connected()) {
            //如果socket链接成功, 则登录的时候判断踢掉别人登录
            String socketId = socket.id();
            Map<String, Object> map = new HashMap<>();
            map.put("id", fu.get("id"));
            map.put("socketId", socketId);
            MyLog.w(TAG, "socket链接成功");
            //登录, 踢下线~
            socket.emit("login", JsonUtil.toJson(map));
            initData();
        } else {
            MyLog.w(TAG, "已经断开, 正在重连");
        }
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            connectionSocket();
            //存储SocketId
            BaseUtils.putSharedPreferences(getApplicationContext(), "socketId", socket.id());
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (!socket.connected()) {
                socket.open();
            }
            Log.e(TAG + "210", "socket链接中断");
        }
    };
    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (!onScreenOff) {
                if (!socket.connected()) {
                    socket.open();
                }
                Log.e(TAG + "220", "socket链接错误");
            }
        }
    };
    private Emitter.Listener onConnectTimeoutError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (!socket.connected()) {
                socket.open();
            }
            Log.e(TAG + "230", "socket链接超时");
        }
    };
    private Emitter.Listener getUserList = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
            l = JsonUtil.toObject(args[0].toString(), List.class);
            for (int i = 0; i < l.size(); i++) {
                for (int j = 0; j < userList.size(); j++) {
                    if (l.get(i).get("id").toString().equals(userList.get(j).get("id").toString())) {
                        userList.get(j).put("online", 1);
                    }
                }
            }
            if (userList.size() > 0) {
                Message msg = new Message();
                msg.what = 0;
                handler1.sendMessage(msg);
            }
        }
    };

    @Override
    protected void initTitle() {
        BaseUtils.initHeaderBar(this);
        headerBar = (HeaderBar) this.findViewById(R.id.headerBar);
        headerBar.setAppTitle("首页");
        headerBar.initViewsVisible(false, true, false, false);
    }

    private void initScreenListener() {
        if (screenListener == null) {
            screenListener = new ScreenListener(this);
            screenListener.begin(new ScreenListener.ScreenStateListener() {
                @Override
                public void onUserPresent() {
                    onScreenOff = false;
                    MyLog.i("onUserPresent", "onUserPresent");
                    socket = BaseApplication.getSocket().open();
                }

                @Override
                public void onScreenOn() {
                    onScreenOff = false;
                    MyLog.i("onScreenOn", "onScreenOn");
                    if (socket != null && !socket.connected()) {
                        socket = BaseApplication.getSocket().open();
                    }
                }

                @Override
                public void onScreenOff() {
                    onScreenOff = true;
                    MyLog.i("onScreenOff", "onScreenOff");
                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                BaseUtils.showToast("再按一次退出应用", context);
                mExitTime = System.currentTimeMillis();
            } else {
                AppManager.getInstance().AppExit(context);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
