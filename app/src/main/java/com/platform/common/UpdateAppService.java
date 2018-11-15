package com.platform.common;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * by Lee
 * 2018年3月21日 13:19:11
 * app自动更新服务
 */
public class UpdateAppService {

    private String url;//需要更新的App地址
    private static Dialog baseDialog = null;//提示框,防止重复弹框

    private long fileLength = 0;//下载的文件长度
    private long contentLength;//文件的总长度
    private String filePath;//文件的保存路径
    private int len;//流长度
    private final Context context;//上下文(new对象时传入)
    private String fileName;//保存到本地的方法名,调用方法时传入

    private int MAX_PROGRESS = 100;//下载进度条最大值
    private ProgressDialog progressDialog = null;//下载进度条对象

    public UpdateAppService(Context context) {
        this.context = context;
    }

    /**
     * 更新app服务
     *
     * @param newVersionCode 新app版本号,用于判断检测更新
     * @param updateInfo     更新内容
     * @param downloadUrl    app文件下载地址
     * @param fileName       app文件下载后名称
     */
    public void checkUpdate(String newVersionCode, String updateInfo, String downloadUrl, String fileName) {
        if (!isNetworkConnected(context)) {
            Toast.makeText(context, "请检查网络链接", Toast.LENGTH_SHORT).show();
            return;
        }
        // 开始检测更新
        if (VersionComparison(newVersionCode, getVersionName()) == 1) {
            this.fileName = fileName;
            url = downloadUrl;
            showUpdateConfirmDialog(updateInfo);
        }
    }

    /**
     * 获取当前app版本号
     *
     * @return
     */
    public String getVersionName() {
        String verName = "";
        try {
            verName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }

    /**
     * 判断网络是否链接
     *
     * @param context
     * @return
     */
    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 显示更新对话框,包含版本相关信息
     */
    private void showUpdateConfirmDialog(final String updateInfo) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (baseDialog == null) {
                    baseDialog = new AlertDialog.Builder(context)
                            .setTitle("发现新版本")
                            .setMessage(updateInfo)
                            .setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    baseDialog = null;
                                    if (isWifi()) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                downLoadApp();
                                            }
                                        }).start();
                                    } else {
                                        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(context);
                                        normalDialog.setMessage("您当前使用的移动网络，是否继续更新?");
                                        normalDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        downLoadApp();
                                                    }
                                                }).start();
                                            }
                                        });
                                        normalDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //点击取消
                                            }
                                        });
                                        // 显示
                                        normalDialog.show();
                                    }

                                    //点击立即更新之后,初始化进度条弹框
                                    progressDialog = new ProgressDialog(context);
                                    progressDialog.setProgress(0);
                                    progressDialog.setTitle("正在更新App,请稍候...");
                                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                    progressDialog.setMax(MAX_PROGRESS);
                                    progressDialog.setCanceledOnTouchOutside(false);
                                    progressDialog.show();
                                }
                            })
                            .show();
                }
            }
        });
    }

    /**
     * 下载文件
     */
    private void downLoadApp() {
        HttpURLConnection urlConnection = null;
        filePath = Environment.getExternalStorageDirectory() + "/" + fileName;
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
                fileLength = file.length();
            }
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setRequestProperty("RANGE", "bytes=" + fileLength + "-");
            urlConnection.setRequestProperty("Charset", "UTF-8");
            urlConnection.connect();
            contentLength = urlConnection.getContentLength();
            if (fileLength < (contentLength < 0 ? 1 : contentLength)) {
                InputStream inputStream = urlConnection.getInputStream();
                FileOutputStream fos = new FileOutputStream(filePath, fileLength > 0);
                byte[] bytes = new byte[1024];
                while ((len = inputStream.read(bytes)) > 0) {
                    fos.write(bytes, 0, len);
                    fileLength += len;
                    Message message1 = new Message();
                    message1.what = 1;
                    handler.sendMessage(message1);
                }
                fos.flush();
                fos.getFD().sync();
                fos.close();
                inputStream.close();
                Message message2 = new Message();
                message2.what = 2;
                handler.sendMessage(message2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
    }

    /**
     * 判断是否是wifi连接
     */
    private boolean isWifi() {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == cm) {
            return false;
        }
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (null != info) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断版本号大小
     *
     * @param versionServer
     * @param versionLocal
     * @return if version1 > version2, return 1, if equal, return 0, else return
     * -1
     */
    private int VersionComparison(String versionServer, String versionLocal) {
        String version1 = versionServer;
        String version2 = versionLocal;
        if (version1 == null || version1.length() == 0 || version2 == null || version2.length() == 0)
            throw new IllegalArgumentException("Invalid parameter!");

        int index1 = 0;
        int index2 = 0;
        while (index1 < version1.length() && index2 < version2.length()) {
            int[] number1 = getValue(version1, index1);
            int[] number2 = getValue(version2, index2);

            if (number1[0] < number2[0]) {
                return -1;
            } else if (number1[0] > number2[0]) {
                return 1;
            } else {
                index1 = number1[1] + 1;
                index2 = number2[1] + 1;
            }
        }
        if (index1 == version1.length() && index2 == version2.length())
            return 0;
        if (index1 < version1.length())
            return 1;
        else
            return -1;
    }

    /**
     * 接上
     *
     * @param version
     * @param index   the starting point
     * @return the number between two dots, and the index of the dot
     */
    private int[] getValue(String version, int index) {
        int[] value_index = new int[2];
        StringBuilder sb = new StringBuilder();
        while (index < version.length() && version.charAt(index) != '.') {
            sb.append(version.charAt(index));
            index++;
        }
        value_index[0] = Integer.parseInt(sb.toString());
        value_index[1] = index;

        return value_index;
    }

    /**
     * handler
     */
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (!Thread.currentThread().isInterrupted()) {
                switch (msg.what) {
                    case 1:
                        double x = ((double) fileLength / (double) contentLength * 100);
                        progressDialog.setProgress(Integer.valueOf((x + "").substring(0, (x + "").indexOf('.'))));
                        if (Integer.valueOf((x + "").substring(0, (x + "").indexOf('.'))) == MAX_PROGRESS) {
                            progressDialog.dismiss();
                        }
                        break;
                    case 2:
                        Toast.makeText(context.getApplicationContext(), "下载完成", Toast.LENGTH_LONG).show();
                        openApk(new File(filePath));
                        /*final AlertDialog.Builder normalDialog = new AlertDialog.Builder(context);
                        normalDialog.setMessage("打开APP");
                        normalDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        normalDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //点击取消
                            }
                        });
                        normalDialog.show();*/
                        break;
                }
            }
        }
    };

    /**
     * 自动安装apk文件
     *
     * @param file
     */
    public void openApk(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 根据文件后缀名匹配MIMEType
     *
     * @param file
     * @return
     */
    public String getMIMEType(File file) {
        String type = "*/*";
        String name = file.getName();
        int index = name.lastIndexOf('.');
        if (index < 0) {
            return type;
        }
        String end = name.substring(index, name.length()).toLowerCase();
        if (TextUtils.isEmpty(end)) return type;

        for (int i = 0; i < MIME_MapTable.length; i++) {
            if (end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }

    /**
     * MIME对象
     */
    private final String[][] MIME_MapTable = {
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop", "text/plain"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/x-zip-compressed"},
            {"", "*/*"}
    };

}


