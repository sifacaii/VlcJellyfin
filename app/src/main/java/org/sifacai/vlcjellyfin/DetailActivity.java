package org.sifacai.vlcjellyfin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class DetailActivity extends BaseActivity implements JAdapter.OnItemClickListener {
    private String TAG = "详情：";
    private String ItemId;
    private ImageView tvCover;
    private TextView tvTitle;
    private TextView tvDetails;
    private ImageView tvPlay;
    private JRecyclerView mGridView;
    private JRecyclerView mPeopleGridView;
    private LinearLayout tvPeopleLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().hide();

        if (JfClient.UserId.equals("") || JfClient.AccessToken.equals("")) {
            finish();
        }

        init();
    }

    private void init() {
        tvCover = findViewById(R.id.tvCover);
        tvTitle = findViewById(R.id.tvTitle);
        tvDetails = findViewById(R.id.tvDetails);
        tvPlay = findViewById(R.id.tvPlay);
        mGridView = findViewById(R.id.mGridView);
        tvPeopleLayout = findViewById(R.id.tvPersonLayout);
        mPeopleGridView = findViewById(R.id.mPersonGridView);

        Intent intent = getIntent();
        ItemId = intent.getStringExtra("itemId");
        if (ItemId.equals("")) {
            finish();
        } else {
            showLoadingDialog("加载中……");
            initData(ItemId);
        }
    }

    private void initData(String itemId) {
        JfClient.GetItemInfo(itemId, new JfClient.JJCallBack() {
            @Override
            public void onSuccess(JsonObject detailObj) {
                fillDetails(detailObj);
            }
        }, new JfClient.JJCallBack() {
            @Override
            public void onError(String str) {
                Log.d(TAG, "onError: " + str);
                finish();
            }
        });
    }

    private void fillDetails(JsonObject detailObj) {
        String Id = detailObj.get("Id").getAsString();
        String Name = JfClient.strFromGson(detailObj, "Name");
        String imgurl = JfClient.GetImgUrl(detailObj);
        Picasso.get()
                .load(imgurl)
                .placeholder(R.drawable.img_loading_placeholder)
                .error(R.drawable.img_loading_placeholder)
                .into(tvCover);

        JsonElement genres = JfClient.jeFromGson(detailObj, "Genres");
        String Genres = genres == null ? "" : genres.getAsJsonArray().toString();
        String OfficialRating = JfClient.strFromGson(detailObj, "OfficialRating");
        String CommunityRating = JfClient.strFromGson(detailObj, "CommunityRating");
        String ProductionYear = JfClient.strFromGson(detailObj, "ProductionYear");
        String Overview = JfClient.strFromGson(detailObj, "Overview");

        tvTitle.setText(Name);
        tvDetails.append(ProductionYear.equals("") ? "" : "年份：" + ProductionYear + "  ");
        tvDetails.append(Genres.equals("") ? "" : "风格：" + Genres + "\n");
        tvDetails.append(CommunityRating.equals("") ? "" : "评分：" + CommunityRating + "  ");
        tvDetails.append(OfficialRating.equals("") ? "" : "评级：" + OfficialRating + "\n");


        JsonArray MediaStreams = null;
        if (detailObj.has("MediaStreams")) {
            String video = "";
            String audio = "";
            String subtitle = "";
            MediaStreams = detailObj.get("MediaStreams").getAsJsonArray();
            for (int i = 0; i < MediaStreams.size(); i++) {
                JsonObject ms = MediaStreams.get(i).getAsJsonObject();
                String mstype = ms.get("Type").getAsString();
                if (mstype.equals("Video")) {
                    video += JfClient.strFromGson(ms, "DisplayTitle");
                } else if (mstype.equals("Audio")) {
                    if (ms.has("Language")) audio += ms.get("Language").getAsString() + "、";
                    else audio += JfClient.strFromGson(ms, "Codec") + "；";
                } else if (mstype.equals("Subtitle")) {
                    if (ms.has("Language")) subtitle += ms.get("Language").getAsString() + "、";
                    else subtitle += JfClient.strFromGson(ms, "Codec") + "；";
                }
            }
            tvDetails.append(video.equals("") ? "" : "视频：" + video + "\n");
            tvDetails.append(audio.equals("") ? "" : "音频：" + audio + "\n");
            tvDetails.append(subtitle.equals("") ? "" : "字幕：" + subtitle + "\n");
        }

        tvDetails.append("简介：  " + Html.fromHtml(Overview) );

        //填充列表
        String type = JfClient.strFromGson(detailObj, "Type");
        if (type.equals("Series")) {
            fillSeason(ItemId);
        } else if (type.equals("Season")) {
            String SeriesId = detailObj.get("SeriesId").getAsString();
            fillEpisodes(SeriesId, ItemId);
        } else if (type.equals("Episode")) {
            String SeriesId = JfClient.strFromGson(detailObj, "SeriesId");
            String SeasonId = JfClient.strFromGson(detailObj, "SeasonId");
            fillEpisodes(SeriesId, SeasonId);
        } else if (type.equals("Movie")) {
            fillMovie(detailObj);
        } else if (type.equals("Person")){
            JsonElement ProductionLocations = JfClient.jeFromGson(detailObj,"ProductionLocations");
            String PremiereDate = JfClient.strFromGson(detailObj,"PremiereDate");
            tvDetails.append("\n出生日期：" +Utils.UtcToLocal(PremiereDate)+"\n");
            tvDetails.append("出生地：" + ProductionLocations == null ? "" : ProductionLocations.toString());
            fillItemsByPerson(Id);
        }

        JsonElement People = JfClient.jeFromGson(detailObj,"People");
        if(People != null){
            JsonArray peoples = People.getAsJsonArray();
            if(peoples.size() > 0) {
                fillPeople(People.getAsJsonArray());
            }
        }
    }

    private void fillMovie(JsonObject item) {
//        tvPlay.setVisibility(View.VISIBLE);
//        tvPlay.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                if (view.hasFocus()) {
//                    view.animate().scaleX(1.05f).scaleY(1.05f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
//                } else {
//                    view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
//                }
//            }
//        });
//        tvPlay.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                JfClient.playList.clear();
//                JfClient.playList.add(getMedia(item));
//                JfClient.playIndex = 0;
//                toVlcPlayer();
//            }
//        });
//        tvPlay.requestFocus();

        String Name = JfClient.strFromGson(item,"Name");
        Name = "播放： " + Name;
        item.remove("Name");
        item.addProperty("Name" ,Name);

        JsonArray plja = new JsonArray();
        plja.add(item);
        if (item.has("PartCount")) {
            String Id = JfClient.strFromGson(item, "Id");
            JfClient.GetAddPart(Id, new JfClient.JJCallBack() {
                @Override
                public void onSuccess(JsonArray parts) {
                    plja.addAll(parts);
                    fillItems(plja);
                }
            }, null);
        } else {
            fillItems(plja);
        }
    }

    /**
     * 填充季
     *
     * @param SeriesId
     */
    private void fillSeason(String SeriesId) {
        JfClient.GetSeasons(SeriesId, new JfClient.JJCallBack() {
            @Override
            public void onSuccess(JsonArray seasons) {
                fillItems(seasons);
            }
        }, null);
    }

    /**
     * 填充集
     *
     * @param SeriesId
     * @param SeasonId
     * @return
     */
    private void fillEpisodes(String SeriesId, String SeasonId) {
        JfClient.GetEpisodes(SeriesId, SeasonId, new JfClient.JJCallBack() {
            @Override
            public void onSuccess(JsonArray episodes) {
                fillItems(episodes);
            }
        }, null);
    }

    private void fillItems(JsonArray items) {
        JAdapter jAdapter = new JAdapter(items, false);
        V7LinearLayoutManager layoutManager = new V7LinearLayoutManager(mGridView.getContext());
        layoutManager.setOrientation(V7LinearLayoutManager.HORIZONTAL);
        jAdapter.setOnItemClickListener(this);
        mGridView.setVisibility(View.VISIBLE);
        mGridView.setLayoutManager(layoutManager);
        mGridView.setAdapter(jAdapter);
        dismissLoadingDialog();
    }

    /**
     * 填充定员表
     * @param items
     */
    private void fillPeople(JsonArray items) {
        tvPeopleLayout.setVisibility(View.VISIBLE);
        JAdapter jAdapter = new JAdapter(items, false);
        V7LinearLayoutManager layoutManager = new V7LinearLayoutManager(mPeopleGridView.getContext());
        layoutManager.setOrientation(V7LinearLayoutManager.HORIZONTAL);
        jAdapter.setOnItemClickListener(this);
        mPeopleGridView.setLayoutManager(layoutManager);
        mPeopleGridView.setAdapter(jAdapter);
    }

    /**
     * 填充演员作品
     * @param personid
     */
    private void fillItemsByPerson(String personid) {
        String Term = "&SortBy=DateCreated&SortOrder=Descending&PersonIds=" + personid;
        JfClient.GetItemsByTerm(Term,new JfClient.JJCallBack(){
            @Override
            public void onSuccess(JsonObject jsonObject) {
                ((TextView)findViewById(R.id.tvListTitle)).setText("演员作品：");
                JsonArray items = jsonObject.get("Items").getAsJsonArray();
                JAdapter jAdapter = new JAdapter(items, false);
                V7GridLayoutManager layoutManager = new V7GridLayoutManager(mGridView.getContext(),4);
                jAdapter.setOnItemClickListener(new JAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(JsonObject jo) {
                        String itemId = JfClient.strFromGson(jo,"Id");
                        Intent intent = new Intent(DetailActivity.this, DetailActivity.class);
                        intent.putExtra("itemId", itemId);
                        startActivity(intent);
                    }
                });
                mGridView.setVisibility(View.VISIBLE);
                mGridView.setLayoutManager(layoutManager);
                mGridView.setAdapter(jAdapter);
                dismissLoadingDialog();
            }
        },new JfClient.JJCallBack(){
            @Override
            public void onError(String str) {
                ShowToask("加载演员作品失败！");
                dismissLoadingDialog();
            }
        });
    }

    @Override
    public void onClick(JsonObject jo) {
        String itemId = jo.get("Id").getAsString();
        String type = jo.get("Type").getAsString();
        Intent intent = null;
        if (type.equals("Season")) {
            intent = new Intent(this, DetailActivity.class);
            intent.putExtra("itemId", itemId);
            startActivity(intent);
        } else if (type.equals("Episode")) {
            JfClient.playList.clear();
            String Id = JfClient.strFromGson(jo, "Id");
            JAdapter JA = (JAdapter) mGridView.getAdapter();
            JsonArray ja = JA.getData();
            if (ja != null) {
                for (int i = 0; i < ja.size(); i++) {
                    Video media = getMedia(ja.get(i).getAsJsonObject());
                    JfClient.playList.add(media);
                    if (Id.equals(media.Id)) {
                        JfClient.playIndex = i;
                    }
                }
                toVlcPlayer();
            }
        } else if (type.equals("Movie") || type.equals("Video")) {
            JfClient.playList.clear();
            JfClient.playList.add(getMedia(jo));
            JfClient.playIndex = 0;
            toVlcPlayer();
        }else if(type.equals("Actor")){
            intent = new Intent(this, DetailActivity.class);
            intent.putExtra("itemId", itemId);
            startActivity(intent);
        }
    }

    /**
     * 组合播放媒体
     *
     * @param item
     * @return
     */
    public Video getMedia(JsonObject item) {
        Video media = new Video();
        media.Id = JfClient.strFromGson(item, "Id");
        media.Name = JfClient.strFromGson(item, "Name");
        media.cover = JfClient.GetImgUrl(item);
        media.Url = JfClient.GetPlayUrl(media.Id);
        if (item.has("UserData")) {
            JsonObject userdata = item.get("UserData").getAsJsonObject();
            media.startPositionTicks = userdata.get("PlaybackPositionTicks").getAsLong();
        }
        return media;
    }

    public void toVlcPlayer() {
        Intent intent = new Intent(this, VlcPlayerActivity.class);
        this.startActivity(intent);
    }
}