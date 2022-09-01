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

import org.sifacai.vlcjellyfin.Bean.ImageTags;
import org.sifacai.vlcjellyfin.Bean.Item;
import org.sifacai.vlcjellyfin.Bean.Items;
import org.sifacai.vlcjellyfin.Bean.MediaStreams;
import org.sifacai.vlcjellyfin.Bean.People;
import org.sifacai.vlcjellyfin.Bean.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
            public void onSuccess(Item item) {
                fillDetails(item);
            }
        }, new JfClient.JJCallBack() {
            @Override
            public void onError(String str) {
                errcb.onError(str);
                finish();
            }
        });
    }

    private void fillDetails(Item details) {
        String Id = details.getId();
        String Name = details.getName();
        String imgurl = JfClient.GetImgUrl(Id, details.getImageTags() == null ? "" : details.getImageTags().getPrimary());
        Picasso.get()
                .load(imgurl)
                .placeholder(R.drawable.img_loading_placeholder)
                .error(R.drawable.img_loading_placeholder)
                .into(tvCover);

        String Genres = String.join(",", details.getGenres());

        tvTitle.setText(Name);
        tvDetails.append(details.getProductionYear().equals("") ? "" : "年份：" + details.getProductionYear() + "  ");
        tvDetails.append(Genres.equals("") ? "" : "风格：" + Genres + "\n");
        tvDetails.append(details.getCommunityRating() == null ? "" : "评分：" + details.getCommunityRating() + "  ");
        tvDetails.append(details.getOfficialRating() == null ? "" : "评级：" + details.getOfficialRating() + "\n");

        if (details.getMediaStreams() != null) {
            String video = "";
            String audio = "";
            String subtitle = "";
            for (int i = 0; i < details.getMediaStreams().size(); i++) {
                MediaStreams ms = details.getMediaStreams().get(i);
                String mstype = ms.getType();
                if (mstype.equals("Video")) {
                    video += ms.getDisplayTitle();
                } else if (mstype.equals("Audio")) {
                    if (!ms.getLanguage().equals("")) audio += ms.getLanguage() + "、";
                    else audio += ms.getCodec() + "；";
                } else if (mstype.equals("Subtitle")) {
                    if (!ms.getLanguage().equals("")) subtitle += ms.getLanguage() + "、";
                    else subtitle += ms.getCodec() + "；";
                }
            }
            tvDetails.append(video.equals("") ? "" : "视频：" + video + "\n");
            tvDetails.append(audio.equals("") ? "" : "音频：" + audio + "\n");
            tvDetails.append(subtitle.equals("") ? "" : "字幕：" + subtitle + "\n");
        }

        tvDetails.append("简介：  " + Html.fromHtml(details.getOverview()));

        //填充列表
        String type = details.getType();
        if (type.equals("Series")) {
            fillSeason(ItemId);
        } else if (type.equals("Season")) {
            String SeriesId = details.getSeriesId();
            fillEpisodes(SeriesId, ItemId);
        } else if (type.equals("Episode")) {
            String SeriesId = details.getSeriesId();
            String SeasonId = details.getSeasonId();
            fillEpisodes(SeriesId, SeasonId);
        } else if (type.equals("Movie")) {
            fillMovie(details);
        } else if (type.equals("Person")) {
            String ProductionLocations = details.getProductionLocations().toString();
            String PremiereDate = String.join(",", details.getPremiereDate());
            tvDetails.append("\n出生日期：" + Utils.UtcToLocal(PremiereDate) + "\n");
            tvDetails.append("出生地：" + (ProductionLocations == null ? "" : ProductionLocations));
            fillItemsByPerson(Id);
        }

        List<People> Peoples = details.getPeople();
        if (Peoples != null) {
            if (Peoples.size() > 0) {
                fillPeople(Peoples);
            }
        }
    }

    private void fillMovie(Item item) {

        item.setName("播放： " + item.getName());
        List<Item> plist = new ArrayList<>();
        plist.add(item);
        if (item.getPartCount() > 0) {
            JfClient.GetAddPart(item.getId(), new JfClient.JJCallBack() {
                @Override
                public void onSuccess(Items parts) {
                    plist.addAll(parts.getItems());
                    fillItems(plist);
                }
            }, null);
        } else {
            fillItems(plist);
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
            public void onSuccess(Items seasons) {
                fillItems(seasons.getItems());
            }
        }, errcb);
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
            public void onSuccess(Items episodes) {
                fillItems(episodes.getItems());
            }
        }, errcb);
    }

    private void fillItems(List<Item> items) {
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
     * 填充演员表
     *
     * @param Peoples
     */
    private void fillPeople(List<People> Peoples) {
        List<Item> Pitems = new ArrayList<>();
        for (People p : Peoples) {
            Item it = new Item();
            it.setId(p.getId());
            if (p.getType().equals("Director")) {
                it.setName("导演：" + p.getName());
            } else if (p.getType().equals("Actor")) {
                it.setName("演员：" + p.getName());
            } else {
                it.setName(p.getName());
            }
            it.setType(p.getType());
            it.setImageTags(new ImageTags());
            it.getImageTags().setPrimary(p.getPrimaryImageTag());
            Pitems.add(it);
        }

        tvPeopleLayout.setVisibility(View.VISIBLE);
        JAdapter jAdapter = new JAdapter(Pitems, false);
        V7LinearLayoutManager layoutManager = new V7LinearLayoutManager(mPeopleGridView.getContext());
        layoutManager.setOrientation(V7LinearLayoutManager.HORIZONTAL);
        jAdapter.setOnItemClickListener(this);
        mPeopleGridView.setLayoutManager(layoutManager);
        mPeopleGridView.setAdapter(jAdapter);
    }

    /**
     * 填充演员作品
     *
     * @param personid
     */
    private void fillItemsByPerson(String personid) {
        String Term = "&SortBy=DateCreated&SortOrder=Descending&PersonIds=" + personid;
        JfClient.GetItemsByTerm(Term, new JfClient.JJCallBack() {
            @Override
            public void onSuccess(Items iitems) {
                ((TextView) findViewById(R.id.tvListTitle)).setText("演员作品：");
                List<Item> items = iitems.getItems();
                JAdapter jAdapter = new JAdapter(items, false);
                V7GridLayoutManager layoutManager = new V7GridLayoutManager(mGridView.getContext(), 4);
                jAdapter.setOnItemClickListener(new JAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(Item item) {
                        String itemId = item.getId();
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
        }, errcb);
    }

    @Override
    public void onClick(Item item) {
        String itemId = item.getId();
        String type = item.getType();
        Intent intent = null;
        if (type.equals("Season")) {
            intent = new Intent(this, DetailActivity.class);
            intent.putExtra("itemId", itemId);
            startActivity(intent);
        } else if (type.equals("Episode")) {
            JfClient.playList.clear();
            JAdapter JA = (JAdapter) mGridView.getAdapter();
            List<Item> ja = JA.getData();
            if (ja != null) {
                for (int i = 0; i < ja.size(); i++) {
                    Video media = getMedia(ja.get(i));
                    JfClient.playList.add(media);
                    if (itemId.equals(media.Id)) {
                        JfClient.playIndex = i;
                    }
                }
                toVlcPlayer();
            }
        } else if (type.equals("Movie") || type.equals("Video")) {
            JfClient.playList.clear();
            JfClient.playList.add(getMedia(item));
            JfClient.playIndex = 0;
            toVlcPlayer();
        } else if (type.equals("Actor") || type.equals("Director")) {
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
    public Video getMedia(Item item) {
        Video media = new Video();
        media.Id = item.getId();
        media.Name = item.getName();
        media.cover = "";
        media.Url = JfClient.GetPlayUrl(media.Id);
        if (item.getUserData() != null) {
            UserData userdata = item.getUserData();
            media.startPositionTicks = userdata.getPlaybackPositionTicks();
        }
        return media;
    }

    public void toVlcPlayer() {
        Intent intent = new Intent(this, VlcPlayerActivity.class);
        this.startActivity(intent);
    }

    private JfClient.JJCallBack errcb = new JfClient.JJCallBack(){
        @Override
        public void onError(String str) {
            ShowToask(str);
            dismissLoadingDialog();
        }
    };
}