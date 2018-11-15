package com.platform.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketIoService extends Service implements Runnable {
    private Socket socket;
    public SocketIoService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        public SocketIoService getMyService() {
            return SocketIoService.this;
        }
    }

    @Override
    public void run() {

    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    /**
     * 向服务器发送请求
     */
    public void connectedSocket(final CallBack callBack) {
        try {
            //1.初始化socket.io，设置链接
            socket = IO.socket("http://192.168.100.72:3000");
            //2.建立socket.io服务器的连接
            socket.connect();
            socket.on("isconnect", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    //保存socket以便全局使用
                    setSocket(socket);
                    for (Object arg : args) {
                        Log.w("81", arg.toString());
                    }
                    //返回socket连接状态
                    Looper.prepare();
                    callBack.getConnectionState(true);
                    Looper.loop();
                }
            });
            socket.on("disconnect", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e("------", "socket链接中断");
                    Looper.prepare();
                    Toast.makeText(SocketIoService.this, "socket连接中断", Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            });
        } catch (URISyntaxException e) {
        }
    }

    /**
     * 退出程序时，关闭Socket连接
     */
    public void closeConnection() {
        try {
            // 向服务器端发送断开连接请求
            socket.emit("exit", true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //在Service销毁时调用
    @Override
    public void onDestroy() {
        Log.w("Service", "Service已经停止");
        super.onDestroy();
    }

    //在创建Service时调用
    @Override
    public void onCreate() {
        Log.w("Service", "Service已经创建");
        super.onCreate();
    }

    //在每次启动Service时调用
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w("Service", "Service已经开启");
        return super.onStartCommand(intent, flags, startId);
    }
}
