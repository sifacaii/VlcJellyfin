package org.sifacai.vlcjellyfin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;
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

public class DetailActivity extends BaseActivity implements JAdapter.OnItemClickListener {
    private String TAG = "详情：";
    private String ItemId;
    private ImageView tvCover;
    private TextView tvTitle;
    private TextView tvGenres;
    private TextView tvRating;
    private TextView tvVideo;
    private TextView tvAudio;
    private TextView tvSubtitle;
    private TextView tvOverview;
    private ImageView tvPlay;
    private JRecyclerView mGridView;

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
        tvGenres = findViewById(R.id.tvGenres);
        tvRating = findViewById(R.id.tvRating);
        tvVideo = findViewById(R.id.tvVideo);
        tvAudio = findViewById(R.id.tvAudio);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvOverview = findViewById(R.id.tvOverview);
        tvPlay = findViewById(R.id.tvPlay);
        mGridView = findViewById(R.id.mGridView);

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
        tvGenres.setText("年份：" + ProductionYear + "  风格：" + Genres);
        tvRating.setText("评分：" + CommunityRating + "  评级：" + OfficialRating);
        tvOverview.setText("简介：    " + Overview);

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
            String finalVideo = video;
            String finalAudio = audio;
            String finalSubtitle = subtitle;
            tvVideo.setText("视频：" + finalVideo);
            tvAudio.setText("音频：" + finalAudio);
            tvSubtitle.setText("字幕：" + finalSubtitle);
        }

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
        }
    }

    private void fillMovie(JsonObject item) {
        tvPlay.setVisibility(View.VISIBLE);
        tvPlay.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (view.hasFocus()) {
                    view.animate().scaleX(1.05f).scaleY(1.05f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
                } else {
                    view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
                }
            }
        });
        tvPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JfClient.playList.clear();
                JfClient.playList.add(getMedia(item));
                JfClient.playIndex = 0;
                toVlcPlayer();
            }
        });
        tvPlay.requestFocus();

        if (item.has("PartCount")) {
            String Id = JfClient.strFromGson(item, "Id");
            JfClient.GetAddPart(Id, new JfClient.JJCallBack() {
                @Override
                public void onSuccess(JsonArray parts) {
                    fillItems(parts);
                }
            }, null);
        } else {
            dismissLoadingDialog();
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

    public void fillItems(JsonArray items) {
        JAdapter jAdapter = new JAdapter(items, false);
        V7LinearLayoutManager layoutManager = new V7LinearLayoutManager(mGridView.getContext());
        layoutManager.setOrientation(V7LinearLayoutManager.HORIZONTAL);
        jAdapter.setOnItemClickListener(this);
        mGridView.setVisibility(View.VISIBLE);
        mGridView.setLayoutManager(layoutManager);
        mGridView.setAdapter(jAdapter);
        dismissLoadingDialog();
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
        }
    }

    /**
     * 组合播放媒体
     *
     * @param item
     * @return
     */
    public Video getMedia(JsonObject item) {
        //String playUrl = Utils.JellyfinUrl + "/videos/" + id + "/stream.mp4?static=true&a";
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