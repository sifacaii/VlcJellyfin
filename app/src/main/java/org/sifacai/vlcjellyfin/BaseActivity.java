package org.sifacai.vlcjellyfin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import me.jessyan.autosize.internal.CustomAdapt;

public class BaseActivity extends AppCompatActivity implements CustomAdapt {
    public AppCompatActivity mAA = this;
    private ProgressDialog progressDialog;
    private ImageView activeBarBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.activebar_custom);

            activeBarBack = findViewById(R.id.activeBar_back);
            activeBarBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAA.finish();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.activebar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.activeBar_option_logout) {
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 禁用标题栏返回按钮
     */
    public void disableActiveBarBack() {
        activeBarBack.setVisibility(View.GONE);
    }

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

    public void showLoadingDialog() {
        showLoadingDialog("");
    }

    /**
     * 显示加载动画框
     */
    public void showLoadingDialog(String title) {
        mAA.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(mAA);
                progressDialog.setMessage(title);
                progressDialog.show();
                progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                            dismissLoadingDialog();
                            mAA.finish();
                            return true;
                        }
                        return false;
                    }
                });
            }
        });
    }

    /**
     * 隐藏加载框
     */
    public void dismissLoadingDialog() {
        mAA.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != progressDialog && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            }
        });
    }

    /**
     * 设置加载框文字
     *
     * @param text
     */
    public void setLoadingText(String text) {
        mAA.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != progressDialog && progressDialog.isShowing()) {
                    progressDialog.setMessage(text);
                }else{
                    showLoadingDialog(text);
                }
            }
        });
    }

    public void ShowToask(String msg) {
        mAA.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mAA, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 读取配置
     */
    public void getConfigFromSP() {
        SharedPreferences sp = this.getSharedPreferences("Jellyfin", this.MODE_PRIVATE);
        Utils.JellyfinUrl = sp.getString("url", "");
        Utils.UserName = sp.getString("username", "");
        Utils.PassWord = sp.getString("password", "");
        Utils.SortBy = sp.getString("sortby","");
        Utils.SortOrder = sp.getString("sortorder","");
    }

    /**
     * 保存配置
     *
     * @param url
     * @param username
     * @param password
     */
    public void saveConfigToSP(String url, String username, String password) {
        SharedPreferences sp = this.getSharedPreferences("Jellyfin", this.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("url", url);
        editor.putString("username", username);
        editor.putString("password", password);
        editor.commit();
    }

    /**
     * 保存单项配置
     */
    public void saveConfigToSP(String key, String value) {
        SharedPreferences sp = this.getSharedPreferences("Jellyfin", this.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * 登 出
     */
    private void logout() {
        SharedPreferences sp = this.getSharedPreferences("Jellyfin", this.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
        Utils.UserId = "";
        Utils.AccessToken = "";
        System.exit(0);
    }
}
