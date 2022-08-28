package org.sifacai.vlcjellyfin;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;

public class SearchActivity extends BaseActivity{
    private TvRecyclerView mGridContiner;
    private JAdapter adapter;
    private final int limit = 24;
    private String BaseUrl;

    private EditText searchTermEdit;
    private ImageView searchBtn;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (Utils.UserId.equals("") || Utils.AccessToken.equals("")) {
            finish();
        }

        ActionBar actionBar = getSupportActionBar();
        if(null != actionBar){
            ImageView acb = actionBar.getCustomView().findViewById(R.id.activeBar_searchBtn);
            if(null != acb){
                acb.setVisibility(View.GONE);
            }
        }

        mGridContiner = findViewById(R.id.mGridView);
        V7GridLayoutManager v7GridLayoutManager = new V7GridLayoutManager(this,6);
        mGridContiner.setLayoutManager(v7GridLayoutManager);
        mGridContiner.setItemAnimator(null);  //防崩溃

        adapter = new JAdapter(new JsonArray());
        mGridContiner.setAdapter(adapter);

        BaseUrl = "/Users/"+Utils.UserId+"/Items?";
        BaseUrl += "Fields=PrimaryImageAspectRatio,CanDelete,BasicSyncInfo,MediaSourceCount";
        BaseUrl += "&Recursive=true&EnableTotalRecordCount=false&ImageTypeLimit=1&IncludePeople=false";
        BaseUrl += "&IncludeMedia=true&IncludeGenres=false&IncludeStudios=false&IncludeArtists=false";
        BaseUrl += "&Limit="+limit;

        searchTermEdit = findViewById(R.id.searchTermEdit);
        searchBtn = findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String st = searchTermEdit.getText().toString().trim();
                if(st.length()>0){
                    Search(st);
                }
            }
        });

        searchTermEdit.requestFocus();
    }

    private void Search(String searchTerm) {
        String movieUrl = BaseUrl + "&searchTerm="+searchTerm+"&IncludeItemTypes=Movie";
        String seriesUrl = BaseUrl + "&searchTerm="+searchTerm+"&IncludeItemTypes=Series";
        String episodeUrl = BaseUrl + "&searchTerm="+searchTerm+"&IncludeItemTypes=Episode";

        new Thread(new Runnable() {
            @Override
            public void run() {
                showLoadingDialog("搜索中………………");
                String movieStr = Utils.okhttpSend(movieUrl);
                String seriesStr = Utils.okhttpSend(seriesUrl);
                JsonObject moviejob = Utils.JsonToObj(movieStr,JsonObject.class);
                JsonObject seriesjob = Utils.JsonToObj(seriesStr,JsonObject.class);
                JsonArray movieItems = moviejob.get("Items").getAsJsonArray();
                JsonArray seriesItems = seriesjob.get("Items").getAsJsonArray();
                movieItems.addAll(seriesItems);
                fillItems(movieItems);
                dismissLoadingDialog();
            }
        }).start();
    }

    private void fillItems(JsonArray items){
        mAA.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.clearItems();
                adapter.addItems(items);
            }
        });
    }
}
