package org.sifacai.vlcjellyfin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import me.jessyan.autosize.internal.CustomAdapt;

public class BaseActivity extends AppCompatActivity implements CustomAdapt {
    public AlertDialog alertDialogLoading;
    public Activity mAA = this;

    /**
     * 是否按照宽度进行等比例适配 (为了保证在高宽比不同的屏幕上也能正常适配, 所以只能在宽度和高度之中选择一个作为基准进行适配)
     *
     * @return {@code true} 为按照宽度进行适配, {@code false} 为按照高度进行适配
     */
    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    /**
     * 返回设计图上的设计尺寸
     * {@link #getSizeInDp} 须配合 {@link #isBaseOnWidth()} 使用, 规则如下:
     * 如果 {@link #isBaseOnWidth()} 返回 {@code true}, {@link #getSizeInDp} 则应该返回设计图的总宽度
     * 如果 {@link #isBaseOnWidth()} 返回 {@code false}, {@link #getSizeInDp} 则应该返回设计图的总高度
     * 如果您不需要自定义设计图上的设计尺寸, 想继续使用在 AndroidManifest 中填写的设计图尺寸, {@link #getSizeInDp} 则返回 {@code 0}
     *
     * @return 设计图上的设计尺寸
     */
    @Override
    public float getSizeInDp() {
        return 0;
    }

    public void showLoadingDialog(String msg) {
        mAA.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showLoadingDialog(1);
                setLoadingText(msg);
            }
        });
    }

    public void showLoadingDialog() {
        mAA.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showLoadingDialog(1);
            }
        });

    }

    /**
     * 显示加载动画框
     */
    public void showLoadingDialog(int i) {
        if (null != alertDialogLoading && alertDialogLoading.isShowing()) {
            alertDialogLoading.dismiss();
        }
        alertDialogLoading = new AlertDialog.Builder(this).create();
        alertDialogLoading.getWindow().setBackgroundDrawable(new ColorDrawable());
        alertDialogLoading.setCancelable(false);
        alertDialogLoading.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    alertDialogLoading.dismiss();
                    mAA.finish();
                    return true;
                }
                return false;
            }
        });
        alertDialogLoading.show();
        alertDialogLoading.setContentView(R.layout.loading_alert);
        alertDialogLoading.setCanceledOnTouchOutside(false);
    }

    public void dismissLoadingDialog() {
        mAA.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissLoadingDialog(1);
            }
        });
    }

    /**
     * 隐藏加载框
     */
    public void dismissLoadingDialog(int i) {
        if (null != alertDialogLoading && alertDialogLoading.isShowing()) {
            alertDialogLoading.dismiss();
        }
    }

    /**
     * 设置加载框文字
     * @param text
     */
    public void setLoadingText(String text){
        mAA.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != alertDialogLoading && alertDialogLoading.isShowing()) {
                    TextView tv = alertDialogLoading.getWindow().getDecorView().findViewById(R.id.progressText);
                    tv.setText(text);
                }
            }
        });
    }
}
