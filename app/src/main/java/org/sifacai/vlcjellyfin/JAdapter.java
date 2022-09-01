package org.sifacai.vlcjellyfin;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.squareup.picasso.Picasso;

import org.sifacai.vlcjellyfin.Bean.Item;
import org.sifacai.vlcjellyfin.Bean.Items;
import org.sifacai.vlcjellyfin.Bean.People;

import java.util.ArrayList;
import java.util.List;

public class JAdapter extends RecyclerView.Adapter {
    private  String TAG = "JAdapter:";
    private List<Item> items;
    private boolean horizon;

    class VH extends RecyclerView.ViewHolder{
        public String id;
        public String type;
        public TextView tvName;
        public ImageView tvCover;
        public SeekBar tvPlayedPercentage;

        public VH(View v) {
            super(v);
            id = "";
            type = "";
            tvName = v.findViewById(R.id.tvName);
            tvCover = v.findViewById(R.id.ivThumb);
            tvPlayedPercentage = v.findViewById(R.id.tvPlayedPercentage);
        }
    }

    public JAdapter(List<Item> items) {
        this.items = items;
        this.horizon = false;
    }

    public JAdapter(List<Item> items,Boolean horizon) {
        this.items = items;
        this.horizon = horizon;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = null;
        if(horizon){
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_h,parent,false);
        }else{
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_v,parent,false);
        }
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        VH v = (VH)holder;
        Item item = items.get(position);
        String SeriesName = item.getSeriesName() == null ? "" : item.getSeriesName();
        String SeasonName = item.getSeasonName() == null ? "" : item.getSeasonName();
        String Name = item.getName();
        String itemid = item.getId();
        v.id = itemid;
        v.tvName.setText(" " + SeriesName + " " + SeasonName + " " + Name);

        if(item.getUserData() != null){
            int pp = (int) Math.round(item.getUserData().getPlayedPercentage());
            v.tvPlayedPercentage.setProgress(pp);
            v.tvPlayedPercentage.setVisibility(View.VISIBLE);
        }
        v.type = item.getType();
        String imgUrl = JfClient.GetImgUrl(item.getId(),item.getImageTags().getPrimary());

        if (!TextUtils.isEmpty(imgUrl)) {
            Picasso.get()
                    .load(imgUrl)
                    .placeholder(R.drawable.img_loading_placeholder)
                    .error(R.drawable.img_loading_placeholder)
                    .into(v.tvCover);
        } else {
            v.tvCover.setImageResource(R.drawable.img_loading_placeholder);
        }
        v.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(item);
            }
        });
    }

    public List<Item> getData(){
        return items;
    }

    @Override
    public int getItemCount() {
        int count = items.size();
        return count;
    }

    /**
     * 设置数据
     * @param items
     */
    public void setItems(List<Item> items){
        this.items = items;
        notifyDataSetChanged();
    }

    /**
     * 添加数据
     * @param items
     */
    public void addItems(List<Item> items){
        int c = this.items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(c,items.size());
    }

    public void clearItems(){
        this.items = new ArrayList<>();
        notifyDataSetChanged();
    }

    //定义OnItemClickListener接口
    public interface OnItemClickListener {
        void onClick(Item item);
    }

    public OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
