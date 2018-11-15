package com.platform.chat;

import android.Manifest;
import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.platform.service.ClientThread;
import com.platform.service.PermissionChecker;

import java.io.ByteArrayOutputStream;

public class MainActivity extends Activity {

    MyHandler handler;
    ClientThread clientThread;
    ByteArrayOutputStream outstream;

    Button start;
    Button stop;
    SurfaceView surfaceView;
    SurfaceHolder sfh;
    Camera camera;
    boolean isPreview = false;        //是否在浏览中
    int screenWidth = 300, screenHeight = 300;
    //权限检测
    private static final String[] RequiredPermissions = new String[]{
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
    };
    protected PermissionChecker permissionChecker = new PermissionChecker();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        handler = new MyHandler();
        clientThread = new ClientThread(handler);
        new Thread(clientThread).start();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;// 获取屏幕分辨率宽度
        screenHeight = dm.heightPixels;

        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        sfh = surfaceView.getHolder();
        sfh.setFixedSize(screenWidth, screenHeight / 4 * 3);
        sfh.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
                                       int arg3) {
                // TODO Auto-generated method stub
            }

            @Override
            public void surfaceCreated(SurfaceHolder arg0) {
                // TODO Auto-generated method stub
                initCamera();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder arg0) {
                if (camera != null) {
                    if (isPreview)
                        camera.stopPreview();
                    camera.release();
                    camera = null;
                }
            }
        });

        //开启连接服务
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                start.setEnabled(false);
            }
        });
    }

    static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x222) {
                //返回信息显示代码
            }
        }
    }

    private void checkPermissions() {
        permissionChecker.verifyPermissions(this, RequiredPermissions, new PermissionChecker.VerifyPermissionsCallback() {
            @Override
            public void onPermissionAllGranted() {
            }

            @Override
            public void onPermissionDeny(String[] permissions) {
                Toast.makeText(MainActivity.this, "Please grant required permissions.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initCamera() {
        if (!isPreview) {
            camera = Camera.open();
            ClientThread.size = camera.getParameters().getPreviewSize();
        }
        if (camera != null && !isPreview) {
            try {
                camera.setPreviewDisplay(sfh);                 // 通过SurfaceView显示取景画面
                Camera.Parameters parameters = camera.getParameters();
                parameters.setPreviewSize(screenWidth, screenHeight / 4 * 3);
                /* 每秒从摄像头捕获5帧画面， */
                parameters.setPreviewFrameRate(5);
                parameters.setPictureFormat(ImageFormat.NV21);           // 设置图片格式
                parameters.setPictureSize(screenWidth, screenHeight / 4 * 3);    // 设置照片的大小
                camera.setDisplayOrientation(90);
                camera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera c) {
                        // TODO Auto-generated method stub
                        Camera.Size size = camera.getParameters().getPreviewSize();
                        try {
                            //调用image.compressToJpeg（）将YUV格式图像数据data转为jpg格式
                            YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                            if (image != null) {
                                Message msg = clientThread.revHandler.obtainMessage();
                                msg.what = 0x111;
                                msg.obj = image;
                                clientThread.revHandler.sendMessage(msg);

                                    /*outstream = new ByteArrayOutputStream();
                                    image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, outstream);
                                    outstream.flush();
                                    new Thread(clientThread).start();*/
                            }
                        } catch (Exception ex) {
                            Log.e("Sys", "Error:" + ex.getMessage());
                        }
                    }
                });
                camera.startPreview();                                   // 开始预览
                camera.autoFocus(null);                                  // 自动对焦
            } catch (Exception e) {
                e.printStackTrace();
            }
            isPreview = true;
        }
    }
}
