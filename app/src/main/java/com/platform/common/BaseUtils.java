package com.platform.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.donkingliang.imageselector.ClipImageActivity;
import com.donkingliang.imageselector.ImageSelectorActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseUtils {

    public static final String KEY_APP_KEY = "JPUSH_APPKEY";

    /**
     * 获取DisplayMetrics
     *
     * @param context
     * @return
     */
    private static DisplayMetrics obtain(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm;
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getDeviceWidth(Context context) {
        DisplayMetrics outMetrics = obtain(context);
        return outMetrics.widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @param context
     * @return
     */
    public static int getDeviceHeight(Context context) {
        DisplayMetrics outMetrics = obtain(context);
        return outMetrics.heightPixels;
    }

    /**
     * 获取屏幕大小[0]宽，[1]高
     *
     * @param context
     * @return
     */
    public static int[] getDeviceSize(Context context) {
        DisplayMetrics outMetrics = obtain(context);
        int[] sizes = new int[2];
        sizes[0] = outMetrics.widthPixels;
        sizes[1] = outMetrics.heightPixels;
        return sizes;
    }

    /**
     * 获取设备屏幕密度dpi，每寸所包含的像素点
     *
     * @param context
     * @return
     */
    public static float getDeviceDensityDpi(Context context) {
        return context.getResources().getDisplayMetrics().densityDpi;
    }

    /**
     * 获取设备屏幕密度,像素的比例
     *
     * @param context
     * @return
     */
    public static float getDeviceDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    /**
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    public static int getStatusHeight(Context context) {
        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    /**
     * 截取当前屏幕画面为bitmap图片
     *
     * @param activity
     * @param hasStatusBar 是否包含当前状态栏,true:包含
     * @return
     */
    public static Bitmap snapCurrentScreenShot(Activity activity, boolean hasStatusBar) {
        View decorView = activity.getWindow().getDecorView();
        decorView.setDrawingCacheEnabled(true);
        decorView.buildDrawingCache();
        Bitmap bmp = decorView.getDrawingCache();
        int deviceSize[] = getDeviceSize(activity);
        int coordinateY = 0;
        int cutHeight = deviceSize[1];
        if (!hasStatusBar) {
            Rect frame = new Rect();
            decorView.getWindowVisibleDisplayFrame(frame);
            coordinateY += frame.top;
            cutHeight -= frame.top;
        }
        Bitmap shot = Bitmap.createBitmap(bmp, 0, coordinateY, deviceSize[0], cutHeight);
        decorView.destroyDrawingCache();
        return shot;
    }

    /**
     * 获取手机IMEI号
     * add <uses-permission android:name="android.permission.READ_PHONE_STATE" /> in AndroidManifest.xml
     *
     * @param context
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getDeviceIMEI(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    /**
     * 获取手机厂商
     *
     * @return
     */
    public static String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }

    /**
     * 获取手机型号
     *
     * @return
     */
    public static String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * 获取手机系统版本号
     *
     * @return
     */
    public static String getDeviceSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 讲px值转变成dip
     *
     * @param context
     * @param px
     * @return
     */
    public static float pxToDip(Context context, float px) {
        return px / getDeviceDensity(context) + 0.5f;
    }

    /**
     * 将dip值转成px
     *
     * @param context
     * @param dip
     * @return
     */
    public static float dipToPx(Context context, float dip) {
        return dip * getDeviceDensity(context) + 0.5f;
    }

    /**
     * 将px值转成sp值
     *
     * @param context
     * @param px
     * @return
     */
    public static float pxToSp(Context context, float px) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return px / fontScale + 0.5f;
    }

    /**
     * 将sp值转成px值
     *
     * @param context
     * @param sp
     * @return
     */
    public static float spTpPx(Context context, float sp) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return sp * fontScale + 0.5f;
    }

    /**
     * 获取当前本地apk的版本
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            //获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取版本号名称
     *
     * @param context 上下文
     * @return
     */
    public static String getVersionName(Context context) {
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
     * 设置本地存储信息
     *
     * @param context
     * @param key
     * @param value
     */
    public static void putSharedPreferences(Context context, String key, String value) {
        //创建SharedPreferences对象
        SharedPreferences sharedPreferences = context.getSharedPreferences("userInfo", context.MODE_PRIVATE);
        //获取Editor对象
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //设置参数
        editor.putString(key, value);
        //提交
        editor.commit();
    }

    /**
     * 获取本地存储信息
     *
     * @param context
     * @param key
     * @return
     */
    public static String getSharedPreferences(Context context, String key) {
        //创建SharedPreferences对象
        SharedPreferences sharedPreferences = context.getSharedPreferences("userInfo", context.MODE_PRIVATE);
        //获取String类型的本地存储信息
        return sharedPreferences.getString(key, null);
    }

    /**
     * 删除本地存储信息
     *
     * @param context
     * @param key
     * @return
     */
    public static void removeSharedPreferences(Context context, String key) {
        //创建SharedPreferences对象
        SharedPreferences sharedPreferences = context.getSharedPreferences("userInfo", context.MODE_PRIVATE);
        //获取Editor对象
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //删除指定key值
        editor.remove(key);
    }

    /**
     * 删除本地存储信息(全部)
     *
     * @param context
     * @return
     */
    public static void deleteSharedPreferences(Context context) {
        //创建SharedPreferences对象
        SharedPreferences sharedPreferences = context.getSharedPreferences("userInfo", context.MODE_PRIVATE);
        //获取Editor对象
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //删除SharedPreferences值
        editor.clear().commit();
    }

    /**
     * 透明导航栏
     *
     * @param activity
     */
    public static void setTransparentBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
                Window window = activity.getWindow();
                View decorView = window.getDecorView();
                //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
                int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                decorView.setSystemUiVisibility(option);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
                //导航栏颜色也可以正常设置
                //window.setNavigationBarColor(Color.TRANSPARENT);
            } else {
                Window window = activity.getWindow();
                WindowManager.LayoutParams attributes = window.getAttributes();
                int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                int flagTranslucentNavigation = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
                attributes.flags |= flagTranslucentStatus;
                //attributes.flags |= flagTranslucentNavigation;
                window.setAttributes(attributes);
            }
        }
    }

    /**
     * 初始化ActionBar
     *
     * @param activity
     */
    public static void initHeaderBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
            //透明状态栏
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        //安卓6.0以上支持
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    /**
     * 退出app
     *
     * @param cont
     */
    public static void exitApp(final Context cont) {
        AlertDialog.Builder builder = new AlertDialog.Builder(cont);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle("确定退出吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //退出
                AppManager.getInstance().AppExit(cont);
            }
        });
        builder.setNegativeButton("再逛逛", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * 解决listView外层包裹ScrollView只显示一行的问题
     *
     * @param listView
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }

    /**
     * 获取appSHA1
     *
     * @param context
     * @return
     */
    public static String getCertificateSHA1Fingerprint(Context context) {
        //获取包管理器
        PackageManager pm = context.getPackageManager();
        //获取当前要获取SHA1值的包名，也可以用其他的包名，但需要注意，
        //在用其他包名的前提是，此方法传递的参数Context应该是对应包的上下文。
        String packageName = context.getPackageName();
        //返回包括在包中的签名信息
        int flags = PackageManager.GET_SIGNATURES;
        PackageInfo packageInfo = null;
        try {
            //获得包的所有内容信息类
            packageInfo = pm.getPackageInfo(packageName, flags);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //签名信息
        Signature[] signatures = packageInfo.signatures;
        byte[] cert = signatures[0].toByteArray();
        //将签名转换为字节数组流
        InputStream input = new ByteArrayInputStream(cert);
        //证书工厂类，这个类实现了出厂合格证算法的功能
        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X509");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //X509证书，X.509是一种非常通用的证书格式
        X509Certificate c = null;
        try {
            c = (X509Certificate) cf.generateCertificate(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String hexString = null;
        try {
            //加密算法的类，这里的参数可以使MD4,MD5等加密算法
            MessageDigest md = MessageDigest.getInstance("SHA1");
            //获得公钥
            byte[] publicKey = md.digest(c.getEncoded());
            //字节到十六进制的格式转换
            hexString = byte2HexFormatted(publicKey);
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
        return hexString;
    }

    //这里是将获取到得编码进行16进制转换
    private static String byte2HexFormatted(byte[] arr) {
        StringBuilder str = new StringBuilder(arr.length * 2);
        for (int i = 0; i < arr.length; i++) {
            String h = Integer.toHexString(arr[i]);
            int l = h.length();
            if (l == 1)
                h = "0" + h;
            if (l > 2)
                h = h.substring(l - 2, l);
            str.append(h.toUpperCase());
            if (i < (arr.length - 1))
                str.append(':');
        }
        return str.toString();
    }

    /**
     * 日期选择
     *
     * @param activity
     * @param themeResId
     * @param tv
     * @param calendar
     */
    public static void showDatePickerDialog(Activity activity, int themeResId, final TextView tv, Calendar calendar) {
        // 直接创建一个DatePickerDialog对话框实例，并将它显示出来
        new DatePickerDialog(activity, themeResId, new DatePickerDialog.OnDateSetListener() {
            // 绑定监听器(How the parent is notified that the date is set.)
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // 此处得到选择的时间，可以进行你想要的操作
                tv.setText(year + "-" + (++monthOfYear) + "-" + dayOfMonth);
            }
        }
                // 设置初始日期
                , calendar.get(Calendar.YEAR)
                , calendar.get(Calendar.MONTH)
                , calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * 时间选择
     *
     * @param activity
     * @param themeResId
     * @param tv
     * @param calendar
     */
    public static void showTimePickerDialog(Activity activity, int themeResId, final TextView tv, Calendar calendar) {
        // Calendar c = Calendar.getInstance();
        // 创建一个TimePickerDialog实例，并把它显示出来
        // 解释一哈，Activity是context的子类
        new TimePickerDialog(activity, themeResId,
                // 绑定监听器
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        tv.setText(hourOfDay + ":" + minute);
                    }
                }
                // 设置初始时间
                , calendar.get(Calendar.HOUR_OF_DAY)
                , calendar.get(Calendar.MINUTE)
                // true表示采用24小时制
                , true).show();
    }

    /**
     * 仿微信选择图片
     * https://www.jianshu.com/p/64a6ed183062
     * 打开相册，选择图片,可多选,不限数量。
     *
     * @param activity
     * @param requestCode
     */
    public static void openPhoto(Activity activity, int requestCode) {
        openPhoto(activity, requestCode, false, 0);
    }

    /**
     * 打开相册，选择图片,可多选,限制最大的选择数量。
     *
     * @param activity
     * @param requestCode
     * @param isSingle       是否单选
     * @param maxSelectCount 图片的最大选择数量，小于等于0时，不限数量，isSingle为false时才有用。
     */
    public static void openPhoto(Activity activity, int requestCode,
                                 boolean isSingle, int maxSelectCount) {
        ImageSelectorActivity.openActivity(activity, requestCode, isSingle, maxSelectCount);
    }

    /**
     * 打开相册，单选图片并剪裁。
     *
     * @param activity
     * @param requestCode
     */
    public static void openPhotoAndClip(Activity activity, int requestCode) {
        ClipImageActivity.openActivity(activity, requestCode);
    }

    /**
     * toString
     *
     * @param o
     * @return
     */
    public static String toString(Object o) {
        if (o == null) {
            return "";
        } else {
            return o.toString();
        }
    }

    /**
     * @param htmlStr
     * @return 删除Html标签
     * @author LongJin
     */
    private static final String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
    private static final String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式
    private static final String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
    private static final String regEx_space = "\\s*|\t|\r|\n";// 定义空格回车换行符
    private static final String regEx_w = "<w[^>]*?>[\\s\\S]*?<\\/w[^>]*?>";//定义所有w标签

    public static String delHTMLTag(String htmlStr) {
        Pattern p_w = Pattern.compile(regEx_w, Pattern.CASE_INSENSITIVE);
        Matcher m_w = p_w.matcher(htmlStr);
        htmlStr = m_w.replaceAll(""); // 过滤script标签


        Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll(""); // 过滤script标签


        Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll(""); // 过滤style标签


        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll(""); // 过滤html标签


        Pattern p_space = Pattern.compile(regEx_space, Pattern.CASE_INSENSITIVE);
        Matcher m_space = p_space.matcher(htmlStr);
        htmlStr = m_space.replaceAll(""); // 过滤空格回车标签


        htmlStr = htmlStr.replaceAll(" ", ""); //过滤
        return htmlStr.trim(); // 返回文本字符串
    }

    /**
     * 更改image 添加属性
     *
     * @param html
     * @return
     */
    public static String updateHtmlImgContent(String html) {
        Document doc_Dis = Jsoup.parse(html);
        Elements ele_Img = doc_Dis.getElementsByTag("img");
        if (ele_Img.size() != 0) {
            for (Element e_Img : ele_Img) {
                e_Img.attr("style", "max-width:100%;height:auto;");
            }
        }
        return doc_Dis.toString();
    }

    /**
     * 判断是否有网络
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
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
     * 判断wifi是否可用
     *
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 判断MOBILE网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * @param context
     * @return
     */
    public static int getConnectedType(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
                return mNetworkInfo.getType();
            }
        }
        return -1;
    }

    /**
     * 判断字符串是否是空的
     *
     * @param s
     * @return
     */
    public static boolean isEmpty(String s) {
        if (null == s)
            return true;
        if (s.length() == 0)
            return true;
        if (s.trim().length() == 0)
            return true;
        return false;
    }

    /**
     * @param toast
     * @param context
     */
    public static void showToast(final String toast, final Context context) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }).start();
    }

    @SuppressLint("MissingPermission")
    public static String getImei(Context context, String imei) {
        String ret = null;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            ret = telephonyManager.getDeviceId();
        } catch (Exception e) {
            Log.v(BaseUtils.class.getSimpleName(), e.getMessage());
        }
        if (isReadableASCII(ret)) {
            return ret;
        } else {
            return imei;
        }
    }

    private static boolean isReadableASCII(CharSequence string) {
        if (TextUtils.isEmpty(string)) return false;
        try {
            Pattern p = Pattern.compile("[\\x20-\\x7E]+");
            return p.matcher(string).matches();
        } catch (Throwable e) {
            return true;
        }
    }

    public static String getDeviceId(Context context) {
        return null;//JPushInterface.getUdid(context);
    }

    // 取得AppKey
    public static String getAppKey(Context context) {
        Bundle metaData = null;
        String appKey = null;
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (null != ai)
                metaData = ai.metaData;
            if (null != metaData) {
                appKey = metaData.getString(KEY_APP_KEY);
                if ((null == appKey) || appKey.length() != 24) {
                    appKey = null;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appKey;
    }

    /**
     * 当scroll和recycle同时出现滚动影响问题
     *
     * @param context
     * @param recyclerView
     */
    public static void scrollAndRecycleViewScroll(Context context, RecyclerView recyclerView) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    /**
     * 获取当前年,月份
     *
     * @return
     */
    public static String getCurrentYearAndMonth() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1);
    }

    /**
     * 获取当前月份
     *
     * @return
     */
    public static String getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        return (calendar.get(Calendar.MONTH) + 1) + "";
    }

    /**
     * 判断time是否在from，to之内
     *
     * @param time 指定日期
     * @param from 开始日期
     * @param to   结束日期
     * @return
     */
    public static boolean belongCalendar(Date time, Date from, Date to) {
        Calendar date = Calendar.getInstance();
        date.setTime(time);

        Calendar after = Calendar.getInstance();
        after.setTime(from);

        Calendar before = Calendar.getInstance();
        before.setTime(to);

        if (date.after(after) && date.before(before)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断网络连接状态
     *
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conn.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }

    /**
     * 格式化时间
     * @param infoTime
     * @return
     */
    public static String transTime(long infoTime) {
        Date data = new Date();
        long l = 24 * 60 * 60 * 1000;
        String transtime = null;
        SimpleDateFormat todayform = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dayform = new SimpleDateFormat("MM-dd HH:mm");
        Calendar cc = Calendar.getInstance();
        Long currentTimertrans = cc.getTimeInMillis();
        int currentWeek = cc.get(Calendar.DAY_OF_WEEK);
        //当日0晨时间
        /*Long currentTimer=cc.getTimeInMillis();*/
        Long currentTimer = data.getTime() - (data.getTime() % l) - 8 * 60 * 60 * 1000;
        int days = (int) ((currentTimer - infoTime) / (24 * 60 * 60 * 1000));//0是一天 1是两天 2是三天
        if (infoTime >= currentTimer) {
            //今天
            transtime = todayform.format(new Date(infoTime));
        } else {
            //不是今天了，在今天之前的
            if ((currentTimer - infoTime) >= 24 * 60 * 60 * 1000) {
                //昨天之前的 之间间隔了一天多 不是昨天  开始进行星期的判断
                if (currentWeek == 1) {
                    //星期天：日期是周六之前的
                    if (days > 5) {
                        //显示月日时间
                        transtime = dayform.format(new Date(infoTime));
                    } else {
                        //显示星期
                        if (days == 5) {
                            //星期一
                            transtime = "星期一 " + todayform.format(new Date(infoTime));
                        } else if (days == 4) {
                            //星期二
                            transtime = "星期二 " + todayform.format(new Date(infoTime));
                        } else if (days == 3) {
                            //星期三
                            transtime = "星期三 " + todayform.format(new Date(infoTime));
                        } else if (days == 2) {
                            //星期四
                            transtime = "星期四 " + todayform.format(new Date(infoTime));
                        } else if (days == 1) {
                            //星期五
                            transtime = "星期五 " + todayform.format(new Date(infoTime));
                        } else if (days == 0) {
                            //星期六
                            transtime = "星期六 " + todayform.format(new Date(infoTime));
                        }
                    }
                } else if (currentWeek == 2) {
                    //星期一:直接显示日期
                    transtime = dayform.format(new Date(infoTime));

                } else if (currentWeek == 3) {
                    //星期二：直接显示日期
                    transtime = dayform.format(new Date(infoTime));
                } else if (currentWeek == 4) {
                    //星期三
                    if (days > 1) {
                        transtime = dayform.format(new Date(infoTime));
                    } else if (days == 1) {
                        transtime = "星期一 " + todayform.format(new Date(infoTime));
                    } else if (days == 0) {
                        //星期六
                        transtime = "星期二 " + todayform.format(new Date(infoTime));
                    }
                } else if (currentWeek == 5) {
                    //星期四
                    if (days > 2) {
                        transtime = dayform.format(new Date(infoTime));
                    } else if (days == 2) {
                        transtime = "星期一 " + todayform.format(new Date(infoTime));
                    } else if (days == 1) {
                        //星期六
                        transtime = "星期二 " + todayform.format(new Date(infoTime));
                    } else if (days == 0) {
                        //星期六
                        transtime = "星期三 " + todayform.format(new Date(infoTime));
                    }
                } else if (currentWeek == 6) {
                    //星期五
                    if (days > 3) {
                        transtime = dayform.format(new Date(infoTime));
                    } else if (days == 3) {
                        transtime = "星期一 " + todayform.format(new Date(infoTime));
                    } else if (days == 2) {
                        //星期六
                        transtime = "星期二 " + todayform.format(new Date(infoTime));
                    } else if (days == 1) {
                        //星期六
                        transtime = "星期三 " + todayform.format(new Date(infoTime));
                    } else if (days == 0) {
                        //星期六
                        transtime = "星期四 " + todayform.format(new Date(infoTime));
                    }
                } else if (currentWeek == 7) {
                    //星期六
                    if (days > 4) {
                        transtime = dayform.format(new Date(infoTime));
                    } else if (days == 4) {
                        transtime = "星期一 " + todayform.format(new Date(infoTime));
                    } else if (days == 3) {
                        //星期六
                        transtime = "星期二 " + todayform.format(new Date(infoTime));
                    } else if (days == 2) {
                        //星期六
                        transtime = "星期三 " + todayform.format(new Date(infoTime));
                    } else if (days == 1) {
                        //星期六
                        transtime = "星期四 " + todayform.format(new Date(infoTime));
                    } else if (days == 1) {
                        //星期六
                        transtime = "星期五 " + todayform.format(new Date(infoTime));
                    }
                }
            } else {
                //是昨天
                transtime = "昨天 " + todayform.format(new Date(infoTime));
            }
        }
        return transtime;
    }
}
