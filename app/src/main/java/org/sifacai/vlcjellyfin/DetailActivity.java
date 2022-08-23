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
import com.google.gson.JsonObject;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DetailActivity extends BaseActivity implements JAdapter.OnItemClickListener{
    private String TAG = "详情：";
    private Activity mActivity;
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

        if (Utils.UserId.equals("") || Utils.AccessToken.equals("")) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        mActivity = this;
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    initData(ItemId);
                }
            }).start();
        }
    }

    private void initData(String itemId) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showLoadingDialog();
            }
        });

        String detailUrl = "/Users/" + Utils.UserId + "/Items/" + itemId;

        String detailStr = Utils.okhttpSend(detailUrl);
        if (detailStr != "") {
            JsonObject detailObj = new Gson().fromJson(detailStr, JsonObject.class);
            String Id = detailObj.get("Id").getAsString();
            String Name = Utils.getJsonString(detailObj, "Name").getAsString();

            if (detailObj.has("ImageTags")) {
                JsonObject ImageTags = detailObj.get("ImageTags").getAsJsonObject();
                if (ImageTags.has("Primary")) {
                    String imgid = ImageTags.get("Primary").getAsString();
                    String finalPicUrl = Utils.getImgUrl(itemId, imgid);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Picasso.get()
                                    .load(finalPicUrl)
                                    .placeholder(R.drawable.img_loading_placeholder)
                                    .error(R.drawable.img_loading_placeholder)
                                    .into(tvCover);
                        }
                    });
                }
            }
            String Genres = Utils.getJsonString(detailObj, "Genres").toString();
            String OfficialRating = Utils.getJsonString(detailObj, "OfficialRating").getAsString();
            String CommunityRating = Utils.getJsonString(detailObj, "CommunityRating").getAsString();
            String ProductionYear = Utils.getJsonString(detailObj, "ProductionYear").getAsString();
            String Overview = Utils.getJsonString(detailObj, "Overview").getAsString();

            String finalGenres = Genres;
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvTitle.setText(Name);
                    tvGenres.setText("年份：" + ProductionYear + "  风格：" + finalGenres);
                    tvRating.setText("评分：" + CommunityRating + "  评级：" + OfficialRating);
                    tvOverview.setText("简介：    " + Overview);
                }
            });

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
                        video += Utils.getJsonString(ms, "DisplayTitle").getAsString();
                    } else if (mstype.equals("Audio")) {
                        if (ms.has("Language")) audio += ms.get("Language").getAsString() + "、";
                        else audio += Utils.getJsonString(ms, "Codec").getAsString() + "；";
                    } else if (mstype.equals("Subtitle")) {
                        if (ms.has("Language")) subtitle += ms.get("Language").getAsString() + "、";
                        else subtitle += Utils.getJsonString(ms, "Codec").getAsString() + "；";
                    }
                }
                String finalVideo = video;
                String finalAudio = audio;
                String finalSubtitle = subtitle;
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvVideo.setText("视频：" + finalVideo);
                        tvAudio.setText("音频：" + finalAudio);
                        tvSubtitle.setText("字幕：" + finalSubtitle);
                    }
                });
            }

            //填充列表
            String type = Utils.getJsonString(detailObj, "Type").getAsString();
            if ( type.equals("Series") ) {
                fillSeason(ItemId);
            }else if( type.equals("Season") ){
                String SeriesId = detailObj.get("SeriesId").getAsString();
                fillEpisodes(SeriesId,ItemId);
            }else if(type.equals("Episode")){
                String SeriesId = Utils.getJsonString(detailObj,"SeriesId").getAsString();
                String SeasonId = Utils.getJsonString(detailObj,"SeasonId").getAsString();
                fillEpisodes(SeriesId,SeasonId);
            }else if( type.equals("Movie") ) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fillMovie(detailObj);
                        dismissLoadingDialog();
                    }
                });
            }
        }
    }

    private void fillMovie(JsonObject item){
        tvPlay.setVisibility(View.VISIBLE);
        tvAudio.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(view.hasFocus()){
                    view.animate().scaleX(1.05f).scaleY(1.05f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
                }else{
                    view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
                }
            }
        });
        tvPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.playList.clear();
                Utils.playList.add(getMedia(item));
                Utils.playIndex = 0;
                toVlcPlayer();
            }
        });
        tvPlay.requestFocus();
    }

    /**
     * 填充季
     *
     * @param SeriesId
     */
    private void fillSeason(String SeriesId) {
        String SeasonsUrl = "/Shows/" + SeriesId + "/Seasons?userId=" + Utils.UserId;
        SeasonsUrl += "&Fields=ItemCounts,PrimaryImageAspectRatio,BasicSyncInfo,MediaSourceCount";
        String SeasonsStr = Utils.okhttpSend(SeasonsUrl);
        ArrayList<String[]> result = new ArrayList<>();
        if (!SeasonsStr.equals("")) {
            JsonObject SeasonsObj = new Gson().fromJson(SeasonsStr, JsonObject.class);
            JsonArray Seasons = SeasonsObj.get("Items").getAsJsonArray();
            JAdapter seasonAdapter = new JAdapter(Seasons, false);
            V7LinearLayoutManager layoutManager = new V7LinearLayoutManager(mGridView.getContext());
            layoutManager.setOrientation(V7LinearLayoutManager.HORIZONTAL);
            seasonAdapter.setOnItemClickListener(this);
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGridView.setVisibility(View.VISIBLE);
                    mGridView.setLayoutManager(layoutManager);
                    mGridView.setAdapter(seasonAdapter);
                    dismissLoadingDialog();
                }
            });
        }
    }

    /**
     * 填充集
     *
     * @param SeriesId
     * @param seasonId
     * @return
     */
    private void fillEpisodes(String SeriesId, String seasonId) {
        String EpisodesUrl = "/Shows/" + SeriesId + "/Episodes?seasonId=" + seasonId;
        EpisodesUrl += "&userId=" + Utils.UserId;
        EpisodesUrl += "&Fields=ItemCounts,PrimaryImageAspectRatio,BasicSyncInfo,CanDelete,MediaSourceCount,Overview";
        String EpisodesStr = Utils.okhttpSend(EpisodesUrl);
        if (!EpisodesStr.equals("")) {
            JsonObject EpisodesObj = new Gson().fromJson(EpisodesStr, JsonObject.class);
            JsonArray Episodes = EpisodesObj.get("Items").getAsJsonArray();
            JAdapter episodeAdapter = new JAdapter(Episodes, false);
            V7LinearLayoutManager layoutManager = new V7LinearLayoutManager(mGridView.getContext());
            layoutManager.setOrientation(V7LinearLayoutManager.HORIZONTAL);
            episodeAdapter.setOnItemClickListener(this);
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGridView.setVisibility(View.VISIBLE);
                    mGridView.setLayoutManager(layoutManager);
                    mGridView.setAdapter(episodeAdapter);
                    dismissLoadingDialog();
                }
            });
        }
    }

    public Video getMedia(JsonObject item) {
        //String playUrl = Utils.JellyfinUrl + "/videos/" + id + "/stream.mp4?static=true&a";
        Video media = new Video();
        media.Id = Utils.getJsonString(item,"Id").getAsString();
        media.Name = Utils.getJsonString(item,"Name").getAsString();
        JsonObject ImageTags = item.get("ImageTags").getAsJsonObject();
        if (ImageTags.has("Primary")) {
            String imgid = ImageTags.get("Primary").getAsString();
            media.cover = Utils.getImgUrl(media.Id, imgid);
        }
        media.Url = Utils.JellyfinUrl + "/videos/" + media.Id + "/stream.mp4?static=true&a";
        if (item.has("UserData")){
            JsonObject userdata = item.get("UserData").getAsJsonObject();
            media.startPositionTicks = userdata.get("PlaybackPositionTicks").getAsLong();
        }

        return media;
    }

    @Override
    public void onClick(JsonObject jo) {
        String itemId = jo.get("Id").getAsString();
        String type = jo.get("Type").getAsString();
        Intent intent = null;
        if(type.equals("Season")){
            intent = new Intent(mActivity,DetailActivity.class);
            intent.putExtra("itemId",itemId);
            mActivity.startActivity(intent);
        }else if(type.equals("Episode")){
            Utils.playList.clear();
            String Id = Utils.getJsonString(jo,"Id").getAsString();
            JAdapter JA = (JAdapter)mGridView.getAdapter();
            JsonArray ja = JA.getData();
            if(ja != null){
                for(int i=0;i<ja.size();i++){
                    Video media = getMedia(ja.get(i).getAsJsonObject());
                    Utils.playList.add(media);
                    if(Id.equals(media.Id)){
                        Utils.playIndex = i;
                    }
                }
                toVlcPlayer();
            }
        }else if(type.equals("Movie")){
            Utils.playList.clear();
            Utils.playList.add(getMedia(jo));
            Utils.playIndex = 0;
            toVlcPlayer();
        }
    }

    public void toVlcPlayer(){
        Intent intent = new Intent(this,VlcPlayerActivity.class);
        this.startActivity(intent);
    }
}