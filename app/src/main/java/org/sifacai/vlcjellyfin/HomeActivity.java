package org.sifacai.vlcjellyfin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;

import org.sifacai.vlcjellyfin.Bean.Item;
import org.sifacai.vlcjellyfin.Bean.Items;

import java.util.List;

public class HomeActivity extends BaseActivity {
    private final String TAG = "HomeActivity";
    private LinearLayout tvContiner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jellyfin_home);

        disableActiveBarBack();

        tvContiner = findViewById(R.id.tvItems);

        JfClient.init(getApplication());
    }

    private JfClient.JJCallBack connErr = new JfClient.JJCallBack() {
        @Override
        public void onError(String str) {
            dismissLoadingDialog();
            ShowToask(str);
            showLoginDialog();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    /**
     * 登录框
     */
    private void showLoginDialog() {
        //Log.d(TAG, "showLoginDialog: 跳出登录框");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog alert = builder.setTitle("登录")
                .setMessage("请输入登录信息")
                .setView(R.layout.dialog_login)
                .show();
        alert.setCanceledOnTouchOutside(false);
        EditText urlbox = alert.findViewById(R.id.dialog_login_url);
        EditText unbox = alert.findViewById(R.id.dialog_login_un);
        EditText pwbox = alert.findViewById(R.id.dialog_login_pw);
        CheckBox saveBox = alert.findViewById(R.id.dialog_login_save);
        TextView submit = alert.findViewById(R.id.dialog_login_submit);
        TextView cancel = alert.findViewById(R.id.dialog_login_cancel);
        urlbox.setText(JfClient.config.getJellyfinUrl());
        unbox.setText(JfClient.config.getUserName());
        pwbox.setText(JfClient.config.getPassWord());
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.exit(0);
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoadingDialog("正在验证服务器地址！");
                JfClient.VerityServerUrl(urlbox.getText().toString(), new JfClient.JJCallBack() {
                    @Override
                    public void onSuccess(Boolean bool) {
                        setLoadingText("正在验证用户名和密码！");
                        JfClient.AuthenticateByName(unbox.getText().toString(), pwbox.getText().toString(), new JfClient.JJCallBack() {
                            @Override
                            public void onSuccess(Boolean bool) {
                                dismissLoadingDialog();
                                alert.dismiss();
                                initView(); //加载首页
                            }
                        }, new JfClient.JJCallBack(){
                            @Override
                            public void onError(String str) {
                                dismissLoadingDialog();
                                ShowToask("用户名密码验证失败！");
                            }
                        }, saveBox.isChecked());

                    }
                }, new JfClient.JJCallBack() {
                    @Override
                    public void onError(String str) {
                        dismissLoadingDialog();
                        ShowToask("服务器地址不正确！");
                    }
                });
            }
        });
    }

    private void initData(){
        if (JfClient.AccessToken.equals("") || JfClient.UserId.equals("")) {
            showLoadingDialog("正在验证服务器地址！");
            JfClient.VerityServerUrl(JfClient.config.getJellyfinUrl(), new JfClient.JJCallBack() {
                @Override
                public void onSuccess(Boolean bool) {
                    setLoadingText("正在验证用户名和密码！");
                    JfClient.AuthenticateByName(JfClient.config.getUserName(), JfClient.config.getPassWord(), new JfClient.JJCallBack() {
                        @Override
                        public void onSuccess(Boolean bool) {
                            dismissLoadingDialog();
                            initView();
                        }
                    }, connErr, false);
                }
            }, connErr);
        }else{
            Log.d(TAG, "initData: 跳出");
            initView();
        }
    }

    private void initView() {
        showLoadingDialog("正在加载首页…………");
        tvContiner.removeAllViews();
        JfClient.GetViews(new JfClient.JJCallBack() {
            @Override
            public void onSuccess(Items views) {
                List<Item> items = views.getItems();
                addRowTvRecyclerView("我的媒体", items, true);
                for (int i = 0; i < items.size(); i++) {
                    Item item = items.get(i);
                    JfClient.GetLatest(item.getId(), new JfClient.JJCallBack() {
                        @Override
                        public void onSuccess(Items latests) {
                            addRowTvRecyclerView("新的 " + item.getName(), latests.getItems(), false);
                        }
                    }, errcb);
                }
                dismissLoadingDialog();
            }
        }, errcb);
        JfClient.GetResume(new JfClient.JJCallBack() {
            @Override
            public void onSuccess(Items resumes) {
                for(Item it : resumes.getItems()){
                    String SeriesName = it.getSeriesName() == null ? "" : it.getSeriesName() + "-";
                    String SeasonName = it.getSeasonName() == null ? "" : it.getSeasonName() + "-";
                    it.setName(SeriesName+SeasonName+it.getName());
                }
                addRowTvRecyclerView("最近播放", resumes.getItems(), false);
            }
        }, errcb);
    }

    /**
     * 添加类别行
     */
    private void addRowTvRecyclerView(String title, List<Item> items, boolean horizon) {
        JRecyclerView tvRecyclerView = (JRecyclerView) LayoutInflater.from(this)
                .inflate(R.layout.home_horizon_tvrecycler, null);
        ((V7LinearLayoutManager) tvRecyclerView.getLayoutManager()).setOrientation(V7LinearLayoutManager.HORIZONTAL);

        JAdapter jAdapter = new JAdapter(items, horizon);
        jAdapter.setOnItemClickListener(new JAdapter.OnItemClickListener() {
            @Override
            public void onClick(Item item) {
                String type = item.getType();
                String itemId = item.getId();
                Intent intent = null;
                if (type.equals("Folder") || type.equals("CollectionFolder")) {
                    intent = new Intent(mAA, CollectionActivity.class);
                } else {
                    intent = new Intent(mAA, DetailActivity.class);
                }
                intent.putExtra("itemId", itemId);
                mAA.startActivity(intent);
            }
        });
        tvRecyclerView.setAdapter(jAdapter);

        TextView titleView = new TextView(tvRecyclerView.getContext());
        titleView.setText(title);
        tvContiner.addView(titleView);
        tvContiner.addView(tvRecyclerView);
        if(title=="我的媒体"){
            tvRecyclerView.requestFocus();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private long exitTime = 0;

    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }

    public JfClient.JJCallBack errcb = new JfClient.JJCallBack(){
        @Override
        public void onError(String str) {
            dismissLoadingDialog();
            ShowToask(str);
        }
    };
}
