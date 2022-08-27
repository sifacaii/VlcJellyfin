package org.sifacai.vlcjellyfin;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
    private Activity mActivity = null;
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

        if (Utils.UserId.equals("") || Utils.AccessToken.equals("")) {
            finish();
        }

        mActivity = this;
        mGridContiner = findViewById(R.id.mGridView);
        tvTitleTip = findViewById(R.id.tvTitleTip);
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

            new Thread(new Runnable() {
                @Override
                public void run() {
                    initData();
                }
            }).start();

            sortMenuBtn = findViewById(R.id.activeBar_sortBtn);
            initSortByMenu(sortMenuBtn);
            sortMenuBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int i = 0;
                    for(Utils.SortByType sbt : Utils.SortByType.values()){
                        if(sbt.value.equals(Utils.SortBy)){
                            i = sbt.ordinal();
                        }
                    }
                    SortByMenu.getMenu().getItem(i).setCheckable(true);
                    SortByMenu.getMenu().getItem(i).setChecked(true);
                    SortByMenu.show();
                }
            });
        }
    }

    public void initData() {
        String url = "/Users/" + Utils.UserId + "/Items/" + ItemId;
        String CollectionStr = Utils.okhttpSend(url);
        JsonObject Collection = Utils.JsonToObj(CollectionStr,JsonObject.class);
        if (null != Collection) {
            currObj = Collection;
            Type = Utils.getJsonString(Collection,"CollectionType").getAsString();
            fillItems();
            setLoadMore();
        }else{
            ShowToask("加载失败！");
        }
    }

    /**
     * 加载条目数据
     */
    private void fillItems(){
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showLoadingDialog();
            }
        });
        String ItemsUrl = "/Users/" + Utils.UserId + "/Items?ParentId=" + ItemId + "&Limit=" + limit;
        ItemsUrl += "&Recursive=true&Fields=PrimaryImageAspectRatio,BasicSyncInfo,Seasons,Episodes&ImageTypeLimit=1";
        ItemsUrl += "&EnableImageTypes=Primary,Backdrop,Banner,Thumb";
        ItemsUrl += "&SortBy="+Utils.SortBy+"%2CSortName%2CProductionYear&SortOrder=" + Utils.SortOrder;
        if (Type.equals("tvshows")) {
            ItemsUrl += "&IncludeItemTypes=Series";
        } else if (Type.equals("movies")) {
            ItemsUrl += "&IncludeItemTypes=Movie";
        } else {
            ItemsUrl += "&IncludeItemTypes=Movie,Series";
        }
        int startIndex = currentPage * limit - limit;
        ItemsUrl += "&StartIndex=" + startIndex;
        String ItemsStr = Utils.okhttpSend(ItemsUrl);
        JsonObject ItemsObj = Utils.JsonToObj(ItemsStr,JsonObject.class);
        if (null != ItemsStr) {
            totalCount = Utils.getJsonString(ItemsObj,"TotalRecordCount").getAsInt();
            countPage = (int) Math.ceil((double) totalCount / limit);

            JsonArray Items = ItemsObj.get("Items").getAsJsonArray();
            int oldcount = currItems.size();
            currItems.addAll(Items);
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissLoadingDialog();
                    //currAdapter.notifyDataSetChanged();
                    currAdapter.notifyItemRangeInserted(oldcount,Items.size());
                    setTitleTip();
                    mGridContiner.finishLoadMore();
                }
            });
        }else{
            ShowToask("加载明细失败！");
        }
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
                    intent = new Intent(mActivity,CollectionActivity.class);
                }else{
                    intent = new Intent(mActivity,DetailActivity.class);
                }
                intent.putExtra("itemId",itemId);
                mActivity.startActivity(intent);
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

    private void initSortByMenu(View view){
        SortByMenu = new PopupMenu(this,view);
        Menu menu = SortByMenu.getMenu();
        Utils.SortByType[] Ss = Utils.SortByType.values();
        for (Utils.SortByType sortby:Ss) {
            menu.add(0,sortby.ordinal(),sortby.ordinal(),sortby.name());
        }
        SortByMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Utils.SortBy = Utils.SortByType.valueOf(menuItem.getTitle().toString()).value;
                currItems = new JsonArray();
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
}