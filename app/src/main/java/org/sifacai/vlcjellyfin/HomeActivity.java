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

public class HomeActivity extends BaseActivity{
    private final String TAG = "HomeActivity";
    private LinearLayout tvContiner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jellyfin_home);

        disableActiveBarBack();

        tvContiner = findViewById(R.id.tvItems);

        JfClient.init(getApplication());

        Log.d(TAG, "onCreate: " + JfClient.config.getJellyfinUrl());
        if(JfClient.AccessToken.equals("") ||JfClient.UserId.equals("")){
            showLoadingDialog("正在验证服务器地址！");
            JfClient.VerityServerUrl(JfClient.config.getJellyfinUrl(),new JfClient.JJCallBack(){
                @Override
                public void onSuccess(Boolean bool) {
                    dismissLoadingDialog();
                    if(bool){
                        showLoadingDialog("正在验证用户名和密码！");
                        JfClient.AuthenticateByName(JfClient.config.getUserName(),JfClient.config.getPassWord(),new JfClient.JJCallBack(){
                            @Override
                            public void onSuccess(Boolean bool) {
                                dismissLoadingDialog();
                                if(bool){
                                    initView(); //加载首页
                                }else{
                                    showLoginDialog();
                                }
                            }
                        },null,false);
                    }else{
                        ShowToask("服务器地址不正确！");
                        showLoginDialog();
                    }
                }
            },new JfClient.JJCallBack(){
                @Override
                public void onError(String str) {
                    dismissLoadingDialog();
                    ShowToask("服务器连接失败！");
                    showLoginDialog();
                }
            });
        }else{
            initView();
        }
    }

    private void showLoginDialog(){
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
                JfClient.VerityServerUrl(urlbox.getText().toString(),new JfClient.JJCallBack(){
                    @Override
                    public void onSuccess(Boolean bool) {
                        dismissLoadingDialog();
                        if(bool){
                            showLoadingDialog("正在验证用户名和密码！");
                            JfClient.AuthenticateByName(unbox.getText().toString(),pwbox.getText().toString(),new JfClient.JJCallBack(){
                                @Override
                                public void onSuccess(Boolean bool) {
                                    dismissLoadingDialog();
                                    if(bool){
                                        alert.dismiss();
                                        initView(); //加载首页
                                    }else{
                                        ShowToask("用户名密码不正确！");
                                    }
                                }
                            },null,saveBox.isChecked());
                        }else{
                            ShowToask("服务器地址不正确！");
                        }
                    }
                },new JfClient.JJCallBack(){
                    @Override
                    public void onError(String str) {
                        dismissLoadingDialog();
                        ShowToask("服务器地址不正确！");
                    }
                });
            }
        });
    }


    private void initView(){
        showLoadingDialog("正在加载首页…………");
        JfClient.GetViews(new JfClient.JJCallBack(){
            @Override
            public void onSuccess(JsonArray views) {
                addRowTvRecyclerView("我的媒体", views, true);
                for(int i=0;i<views.size();i++){
                    JsonObject colls = views.get(i).getAsJsonObject();
                    String name = JfClient.strFromGson(colls,"Name");
                    String Id = JfClient.strFromGson(colls,"Id");
                    JfClient.GetLatest(Id,new JfClient.JJCallBack(){
                        @Override
                        public void onSuccess(JsonArray latests) {
                            addRowTvRecyclerView("新的 " + name,latests,false);
                        }
                    },null);
                }
                dismissLoadingDialog();
            }
        },null);
        JfClient.GetResume(new JfClient.JJCallBack(){
            @Override
            public void onSuccess(JsonArray resumes) {
                addRowTvRecyclerView("最近播放",resumes,false);
            }
        },null);
    }


    /**
     * 添加类别行
     */
    private void addRowTvRecyclerView(String title, JsonArray data, boolean horizon) {
        JRecyclerView tvRecyclerView = (JRecyclerView) LayoutInflater.from(this)
                .inflate(R.layout.home_horizon_tvrecycler, null);
        ((V7LinearLayoutManager) tvRecyclerView.getLayoutManager()).setOrientation(V7LinearLayoutManager.HORIZONTAL);

        JAdapter jAdapter = new JAdapter(data, horizon);
        jAdapter.setOnItemClickListener(new JAdapter.OnItemClickListener() {
            @Override
            public void onClick(JsonObject jo) {
                String type = JfClient.strFromGson(jo, "Type");
                String itemId = jo.get("Id").getAsString();
                Intent intent = null;
                if (type.equals("Folder") || type.equals("CollectionFolder")) {
                    intent = new Intent(mAA, CollectionActivity2.class);
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
}
