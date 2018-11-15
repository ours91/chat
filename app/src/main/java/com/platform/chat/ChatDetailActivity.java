package com.platform.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.insplatform.core.utils.JsonUtil;
import com.insplatform.core.utils.TextUtil;
import com.platform.adapter.UserDetailMessageAdapter;
import com.platform.application.BaseApplication;
import com.platform.common.BaseActivity;
import com.platform.common.BaseUtils;
import com.platform.common.HeaderBar;
import com.platform.common.MyLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatDetailActivity extends BaseActivity {
    private final String TAG = "ChatDetailActivity";
    private Socket socket;
    private HeaderBar headerBar;
    private RecyclerView recyclerView;
    private UserDetailMessageAdapter userDetailMessageAdapter;
    private String nickName;
    private String thatUserId;
    private ImageButton send_button;
    private EditText message_input;
    private List<Map<String, Object>> messageList;
    private Map<String, Object> fu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);
        Bundle bundle = getIntent().getExtras();
        nickName = bundle.getCharSequence("name").toString();
        thatUserId = bundle.getCharSequence("id").toString();
        initTitle();
        init();
        initData();
    }

    @Override
    protected void init() {
        send_button = $(R.id.send_button);
        message_input = $(R.id.message_input);
        send_button.setOnClickListener(l);
        recyclerView = (RecyclerView) findViewById(R.id.messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fu = JsonUtil.toObject(BaseUtils.getSharedPreferences(context, "fu"), Map.class);
        messageList = new ArrayList<Map<String, Object>>();
        BaseApplication app = (BaseApplication) getApplication();
        socket = app.getSocket();
        socket.open();
        if (socket.connected()) {
            MyLog.e("-----*****", "socket在线");
        } else {
            MyLog.e("-----", "socket已掉线");
        }
//        socket.off("message");
        socket.on("message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                MyLog.w(TAG + "------", args[0].toString());
                Map<String, Object> m = JsonUtil.toObject(args[0].toString(), Map.class);
                if (!fu.get("nickName").equals(m.get("nickName").toString())) {
                    messageList.add(m);
                }
                if (messageList.size() > 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Message msg = new Message();
                            msg.what = 0;
                            handler.sendMessage(msg);
                        }
                    }).start();
                }
            }
        });
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.off("message");
    }

    View.OnClickListener l = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.send_button:
                    sendMessage(message_input.getText().toString());
                    break;
            }
        }
    };

    private void sendMessage(String text) {
        if (TextUtil.isEmpty(text)) {
            BaseUtils.showToast("发送信息不能为空", context);
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("nickName", fu.get("nickName"));
        map.put("message", text);
        map.put("photo", fu.get("photo"));
        map.put("who", 0);
        messageList.add(map);
        if (messageList.size() > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = new Message();
                    msg.what = 0;
                    handler.sendMessage(msg);
                }
            }).start();
        }
        BaseApplication app = (BaseApplication) getApplication();
        socket = app.getSocket();
        socket.open();
        Map<String, Object> message = new HashMap<>();
        message.put("thatUserId", thatUserId);
        message.put("nickName", fu.get("nickName"));
        message.put("message", text);
        message.put("photo", fu.get("photo"));
        message.put("who", 1);
        if (socket.connected()) {
            socket.emit("message", JsonUtil.toJson(message));
        } else {
            MyLog.e("-----", "socket已掉线");
        }
        message_input.setText("");
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    setValue();
                    break;
            }
            return false;
        }
    });

    private void setValue() {
        userDetailMessageAdapter = new UserDetailMessageAdapter(context, R.layout.adapter_user_detail_message, messageList);
        recyclerView.setAdapter(userDetailMessageAdapter);
    }

    @Override
    protected void initTitle() {
        BaseUtils.initHeaderBar(this);
        headerBar = (HeaderBar) this.findViewById(R.id.headerBar);
        headerBar.setAppTitle(nickName);
        headerBar.initViewsVisible(true, true, false, false);
        headerBar.setOnLeftButtonClickListener(new HeaderBar.OnLeftButtonClickListener() {
            @Override
            public void onLeftButtonClick(View v) {
                onBackPressed();
            }
        });
    }
}
