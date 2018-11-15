package com.platform.common;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;


public class DialogUtils {

    public static void showAlertDialog(Context context, String msg) {
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(context);
        normalDialog.setMessage(msg);
        normalDialog.setNegativeButton("我知道了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        // 显示
        normalDialog.show();
    }

    public static void showAlertDialog(Context context, String msg, DialogInterface.OnClickListener l1) {
        showAlertDialog(context, msg, "确定", l1, "取消");
    }

    public static void showAlertDialog(Context context, String msg, String btn1, DialogInterface.OnClickListener l1) {
        showAlertDialog(context, msg, btn1, l1, "取消");
    }

    public static void showAlertDialog(Context context, String msg, DialogInterface.OnClickListener l1, DialogInterface.OnClickListener l2) {
        showAlertDialog(context, msg, "确定", l1, "取消", l2);
    }

    public static void showAlertDialog(Context context, String msg, String btn1, DialogInterface.OnClickListener l1, String btn2, DialogInterface.OnClickListener l2) {
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(context);
        normalDialog.setMessage(msg);
        normalDialog.setPositiveButton(btn1, l1);
        normalDialog.setNegativeButton(btn2, l2);
        // 显示
        normalDialog.show();
    }

    public static void showAlertDialog(Context context, String msg, String btn1, DialogInterface.OnClickListener l1, String btn2) {
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(context);
        normalDialog.setMessage(msg);
        normalDialog.setPositiveButton(btn1, l1);
        normalDialog.setNegativeButton(btn2, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //点击取消
            }
        });
        // 显示
        normalDialog.show();
    }
}
