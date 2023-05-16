package org.sifacai.vlcjellyfin.Ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;
import com.squareup.picasso.Picasso;

import org.sifacai.vlcjellyfin.Bean.ImageTags;
import org.sifacai.vlcjellyfin.Bean.Item;
import org.sifacai.vlcjellyfin.Bean.Items;
import org.sifacai.vlcjellyfin.Bean.MediaStreams;
import org.sifacai.vlcjellyfin.Bean.People;
import org.sifacai.vlcjellyfin.Bean.UserData;
import org.sifacai.vlcjellyfin.Component.JAdapter;
import org.sifacai.vlcjellyfin.Component.JRecyclerView;
import org.sifacai.vlcjellyfin.Component.JTAdapter;
import org.sifacai.vlcjellyfin.Utils.JfClient;
import org.sifacai.vlcjellyfin.R;
import org.sifacai.vlcjellyfin.Utils.Utils;
import org.sifacai.vlcjellyfin.Player.Video;
import org.sifacai.vlcjellyfin.Player.VlcPlayerActivity;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends BaseActivity implements JAdapter.OnItemClickListener {
    private String TAG = "详情：";
    private String ItemId;
    private ImageView tvCover;
    private TextView tvTitle;
    private TextView tvDetails;
    private JRecyclerView mGridView;
    private JRecyclerView mPeopleGridView;
    private LinearLayout tvPeopleLayout;
    private TabLayout tabContainer;
    private Item currentItem;

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
        mGridView = findViewById(R.id.mGridView);
        tvPeopleLayout = findViewById(R.id.tvPersonLayout);
        mPeopleGridView = findViewById(R.id.mPersonGridView);
        tabContainer = findViewById(R.id.tab_container);

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
                String type = item.getType();
                if (type.equals("Season") || type.equals("Episode")) {
                    currentItem = item;
                    initData(item.getSeriesId());
                } else {
                    fillDetails(item);
                }
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

        List<String> backdrops = details.getBackdropImageTags();
        if(backdrops != null && backdrops.size() > 0){
            Picasso.get()
                    .load(JfClient.GetBackdropUrl(Id, backdrops.get(0)))
                    .placeholder(R.drawable.img_loading_placeholder)
                    .error(R.drawable.img_loading_placeholder)
                    .into((ImageView) findViewById(R.id.tvBackdrop));
        }

        tvTitle.setText(Name);
        ((TextView)findViewById(R.id.tvYear)).setText(details.getProductionYear() == null ? "" : details.getProductionYear());
        long duration =  (details.getRunTimeTicks() / 10000 / 1000 / 60);
        ((TextView)findViewById(R.id.tvDuration)).setText(duration > 0 ? String.valueOf(duration) + "分钟" : "");
        ((TextView)findViewById(R.id.tvRating)).setText(details.getCommunityRating() == null ? "" : details.getCommunityRating());
        ((TextView)findViewById(R.id.tvLevel)).setText(details.getOfficialRating() == null ? "" : details.getOfficialRating());

        String Genres = String.join("，", details.getGenres());
        tvDetails.append(Genres.equals("") ? "" : "风格：" + Genres + "\n");
        if (details.getMediaStreams() != null) {
            String video = "";
            ArrayList<String> audio = new ArrayList<>();
            ArrayList<String> subtitle = new ArrayList<>();
            for (int i = 0; i < details.getMediaStreams().size(); i++) {
                MediaStreams ms = details.getMediaStreams().get(i);
                String mstype = ms.getType();
                if (mstype.equals("Video")) {
                    video += ms.getDisplayTitle();
                } else if (mstype.equals("Audio")) {
                    if (ms.getLanguage() != null && !ms.getLanguage().equals(""))
                        audio.add(ms.getLanguage());
                } else if (mstype.equals("Subtitle")) {
                    if (ms.getLanguage() != null && !ms.getLanguage().equals(""))
                        subtitle.add(ms.getLanguage());
                }
            }
            tvDetails.append(video.equals("") ? "" : "格式：" + video + "\n");
            tvDetails.append(audio.size() > 1 ? "音轨：" + String.join("，",audio) + "\n" : "");
            tvDetails.append(subtitle.size() > 1 ? "字幕：" + String.join("，",subtitle) + "\n" : "");
        }
        String overview = details.getOverview() == null ? "" : details.getOverview();
        tvDetails.append("简介：  " + Html.fromHtml(overview));

        //填充列表
        String type = details.getType();
        if (type.equals("Series")) {
            fillSeason(details.getId());
        } else if (type.equals("Movie")) {
            fillMovie(details);
        } else if (type.equals("Person")) {
            tvDetails.append("\n出生日期：" + Utils.UtcToLocal(details.getPremiereDate()) + "\n");
            if (null != details.getProductionLocations()) {
                tvDetails.append("出生地：" + String.join(",", details.getProductionLocations()));
            }
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
        item.setName(item.getName());
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
                for (int i=0;i<seasons.getItems().size();i++) {
                    Item item = seasons.getItems().get(i);
                    TabLayout.Tab tab = tabContainer.newTab();
                    tab.setText(item.getName());
                    tab.view.setTag(item);
                    tab.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Item it = (Item) view.getTag();
                            fillEpisodes(it.getSeriesId(), it.getId());
                        }
                    });
                    tabContainer.addTab(tab);
                    if (currentItem != null && currentItem.getSeasonId() != null) {
                        if (item.getId().equals(currentItem.getSeasonId())) {
                            tab.view.setSelected(true);
                            tab.view.performClick();
                        }
                    }else if(i == 0){
                        tabContainer.getTabAt(0).view.performClick();
                    }
                    //tabContainer.getTabAt(0).view.setFocusable(false);
                }
                if(tabContainer.getTabCount() > 1){
                    tabContainer.setVisibility(View.VISIBLE);
                }
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
        int spanCount = 3;
        if(getResources().getDisplayMetrics().widthPixels >getResources().getDisplayMetrics().heightPixels ){
            spanCount = 6;
        }
        JTAdapter jtAdapter = new JTAdapter(items);
        V7GridLayoutManager layoutManager = new V7GridLayoutManager(mGridView.getContext(), spanCount);
        jtAdapter.setOnItemClickListener(this);
        mGridView.setVisibility(View.VISIBLE);
        mGridView.setLayoutManager(layoutManager);
        mGridView.setAdapter(jtAdapter);
        dismissLoadingDialog();
        mGridView.requestFocus();
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
        Term += "&IncludeItemTypes=Movie,Series";
        JfClient.GetItemsByTerm(Term, new JfClient.JJCallBack() {
            @Override
            public void onSuccess(Items iitems) {
                ((TextView) findViewById(R.id.tvListTitle)).setText("演员作品：");
                List<Item> items = iitems.getItems();
                JAdapter jAdapter = new JAdapter(items, false);
                V7GridLayoutManager layoutManager = new V7GridLayoutManager(mGridView.getContext(), getSpanCount());
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
                //mGridView.requestFocus();
            }
        }, errcb);
    }

    @Override
    public void onClick(Item item) {
        String itemId = item.getId();
        String type = item.getType();
        Intent intent = null;
        if (type.equals("Episode")) {
            JfClient.playList.clear();
            JTAdapter JT = (JTAdapter) mGridView.getAdapter();
            List<Item> ja = JT.getData();
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
        Intent intent;
        if (JfClient.config.isExtensionPlayer()) {
            String videourl = JfClient.playList.get(JfClient.playIndex).Url;
            Uri uri = Uri.parse(videourl);
            intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "video/mp4");
        } else {
            intent = new Intent(this, VlcPlayerActivity.class);
        }
        this.startActivity(intent);
    }

    private JfClient.JJCallBack errcb = new JfClient.JJCallBack() {
        @Override
        public void onError(String str) {
            ShowToask(str);
            dismissLoadingDialog();
        }
    };
}