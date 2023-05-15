package org.sifacai.vlcjellyfin.Ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;

import org.sifacai.vlcjellyfin.Bean.Item;
import org.sifacai.vlcjellyfin.Bean.Items;
import org.sifacai.vlcjellyfin.Component.JAdapter;
import org.sifacai.vlcjellyfin.Utils.JfClient;
import org.sifacai.vlcjellyfin.R;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity implements JAdapter.OnItemClickListener {
    private TvRecyclerView mGridView;
    private JAdapter adapter;
    private final int limit = 24;
    private String BaseUrl;

    private EditText searchTermEdit;
    private ImageView searchBtn;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (JfClient.UserId.equals("") || JfClient.AccessToken.equals("")) {
            finish();
        }

        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            ImageView acb = actionBar.getCustomView().findViewById(R.id.actionBar_searchBtn);
            if (null != acb) {
                acb.setVisibility(View.GONE);
            }
        }

        mGridView = findViewById(R.id.mGridView);
        V7GridLayoutManager v7GridLayoutManager = new V7GridLayoutManager(this, getSpanCount());
        mGridView.setLayoutManager(v7GridLayoutManager);
        mGridView.setItemAnimator(null);  //防崩溃

        adapter = new JAdapter(new ArrayList<>());
        adapter.setOnItemClickListener(this);
        mGridView.setAdapter(adapter);

        searchTermEdit = findViewById(R.id.searchTermEdit);
        searchBtn = findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String st = searchTermEdit.getText().toString().trim();
                if (st.length() > 0) {
                    adapter.clearItems();
                    Search(st);
                }
            }
        });

        searchTermEdit.requestFocus();
    }

    private void Search(String searchTerm) {
        showLoadingDialog("搜索中………………");
        JfClient.SearchByTerm(searchTerm,16,new JfClient.JJCallBack(){
            @Override
            public void onSuccess(Items items) {
                mAA.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.addItems(items.getItems());
                    }
                });
                dismissLoadingDialog();
            }
        },new JfClient.JJCallBack(){
            @Override
            public void onError(String str) {
                ShowToask("搜索时发生错误：" + str);
                dismissLoadingDialog();
            }
        });
    }

    private void fillItems(List<Item> items) {
        mAA.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.addItems(items);
            }
        });
    }

    @Override
    public void onClick(Item item) {
        String itemId = item.getId();
        String type = item.getType();
        Intent intent = null;
        switch (type) {
            case "Series":
            case "Season":
            case "Episode":
            case "Movie":
            case "Video":
            case "Person":
                intent = new Intent(this, DetailActivity.class);
                intent.putExtra("itemId", itemId);
                this.startActivity(intent);
                break;
            default:
                ShowToask("未知类型！");
        }
    }
}
