package org.sifacai.vlcjellyfin;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;

public class SearchActivity extends BaseActivity{
    private TvRecyclerView mGridContiner = null;
    private final int limit = 24;
    private String BaseUrl;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        if (Utils.UserId.equals("") || Utils.AccessToken.equals("")) {
            finish();
        }

        mGridContiner = findViewById(R.id.mGridView);
        V7GridLayoutManager v7GridLayoutManager = new V7GridLayoutManager(this,6);
        mGridContiner.setLayoutManager(v7GridLayoutManager);
        mGridContiner.setItemAnimator(null);  //防崩溃

        BaseUrl = "/Users/"+Utils.UserId+"/Items?";
        BaseUrl += "Fields=PrimaryImageAspectRatio,CanDelete,BasicSyncInfo,MediaSourceCount";
        BaseUrl += "&Recursive=true&EnableTotalRecordCount=false&ImageTypeLimit=1&IncludePeople=false";
        BaseUrl += "&IncludeMedia=true&IncludeGenres=false&IncludeStudios=false&IncludeArtists=false";
        BaseUrl += "&Limit="+limit;
    }

    private void Search(String searchTerm) {
        String url = BaseUrl + "&searchTerm="+searchTerm+"&IncludeItemTypes=Movie";




    }
}
