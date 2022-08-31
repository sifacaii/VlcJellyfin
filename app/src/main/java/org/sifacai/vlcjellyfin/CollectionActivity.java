package org.sifacai.vlcjellyfin;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;

public class CollectionActivity extends BaseActivity {
    private String TAG = "CollectionActivity";
    private TvRecyclerView mGridContiner = null;
    private TextView tvTitleTip = null;
    private String ItemId = "";
    private int currentPage = 1; //当前页码
    private int countPage = 1;   //总页数
    private int limit = 60;      //每页条目
    private int totalCount = 0;  //总条目数
    private String Type = "";
    private JsonObject currObj = null;
    private JsonArray currItems = null;
    private JAdapter currAdapter = null;

    private TextView sortMenuBtn;

    private PopupMenu SortByMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        if (JfClient.UserId.equals("") || JfClient.AccessToken.equals("")) {
            finish();
        }

        mGridContiner = findViewById(R.id.mGridView);
        tvTitleTip = findViewById(R.id.activeBar_titleTip);
        V7GridLayoutManager v7GridLayoutManager = new V7GridLayoutManager(this,6);
        mGridContiner.setLayoutManager(v7GridLayoutManager);
        mGridContiner.setItemAnimator(null);  //防崩溃
        init();
    }

    private void init() {
        Intent intent = getIntent();
        ItemId = intent.getStringExtra("itemId");
        if(ItemId.equals("")){
            finish();
        }else{
            currItems = new JsonArray();
            currAdapter = getJAdapter(currItems);
            mGridContiner.setAdapter(currAdapter);

            initData();

            initSortByMenu();
        }
    }

    public void initData() {
        showLoadingDialog("加载中…………");
        JfClient.GetItemInfo(ItemId,new JfClient.JJCallBack(){
            @Override
            public void onSuccess(JsonObject Collection) {
                currObj = Collection;
                Type = JfClient.strFromGson(Collection,"CollectionType");
                fillItems();
                setLoadMore();
            }
        },null);
    }

    /**
     * 加载条目数据
     */
    private void fillItems(){
        JfClient.GetCollection(ItemId,Type,JfClient.config.getSortBy(),JfClient.config.getSortOrder(),limit,currentPage,new JfClient.JJCallBack(){
            @Override
            public void onSuccess(JsonObject items) {
                totalCount = JfClient.jeFromGson(items,"TotalRecordCount").getAsInt();
                countPage = (int) Math.ceil((double) totalCount / limit);
                JsonArray Items = items.get("Items").getAsJsonArray();
                dismissLoadingDialog();
                currAdapter.addItems(Items);
                setTitleTip();
                mGridContiner.finishLoadMore();
            }
        },null);
    }

    private JAdapter getJAdapter(JsonArray items){
        JAdapter jAdapter = new JAdapter(items,false);
        jAdapter.setOnItemClickListener(new JAdapter.OnItemClickListener() {
            @Override
            public void onClick(JsonObject jo) {
                String type = Utils.getJsonString(jo,"Type").getAsString();
                String itemId = jo.get("Id").getAsString();
                Intent intent = null;
                if(type.equals("Folder") || type.equals("CollectionFolder")){
                    intent = new Intent(mAA,CollectionActivity.class);
                }else{
                    intent = new Intent(mAA,DetailActivity.class);
                }
                intent.putExtra("itemId",itemId);
                mAA.startActivity(intent);
            }
        });
        return jAdapter;
    }

    private void setLoadMore(){
        mGridContiner.setOnLoadMoreListener(new TvRecyclerView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if(currentPage < countPage){
                    currentPage += 1;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            fillItems();
                        }
                    }).start();
                }
            }
        });
    }

    private void setTitleTip(){
        String tip = "共 "+ totalCount +" ，" + countPage + " 页，已加载" + currentPage + "页";
        tvTitleTip.setText(tip);
    }

    private void initSortByMenu(){
        sortMenuBtn = findViewById(R.id.activeBar_sortBtn);
        sortMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int i = 0;
                for(Config.SortByType sbt : Config.SortByType.values()){
                    if(sbt.value.equals(Utils.config.getSortBy())){
                        i = sbt.ordinal();
                    }
                }
                SortByMenu.show();
            }
        });

        sortMenuBtn.setVisibility(View.VISIBLE);
        setSortMenuBtnText();
        SortByMenu = new PopupMenu(this,sortMenuBtn);
        Menu menu = SortByMenu.getMenu();
        Config.SortByType[] Ss = Config.SortByType.values();
        for (Config.SortByType sortby:Ss) {
            menu.add(0,sortby.ordinal(),sortby.ordinal(),sortby.name());
        }
        for (Config.SotrOrderType sot:Config.SotrOrderType.values()){
            menu.add(1,sot.ordinal() + Ss.length,sot.ordinal() + Ss.length,sot.name());
        }
        SortByMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getGroupId() == 0) {
                    Utils.config.setSortBy(Config.SortByType.valueOf(menuItem.getTitle().toString()).value);
                }else{
                    Utils.config.setSortOrder(Config.SotrOrderType.valueOf(menuItem.getTitle().toString()).value);
                }
                setSortMenuBtnText();
                currAdapter.clearItems();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        initData();
                    }
                }).start();
                return false;
            }
        });
    }

    private void setSortMenuBtnText(){
        String s = Config.SortByType.findName(Utils.config.getSortBy());
        s += "-";
        s += Config.SotrOrderType.findName(Utils.config.getSortOrder());
        sortMenuBtn.setText(s);
    }
}