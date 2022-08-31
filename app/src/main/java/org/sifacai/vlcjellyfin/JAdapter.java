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

public class JAdapter extends RecyclerView.Adapter {
    private  String TAG = "JAdapter:";
    private JsonArray items;
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

    public JAdapter(JsonArray items) {
        this.items = items;
        this.horizon = false;
    }

    public JAdapter(JsonArray items,Boolean horizon) {
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
        JsonObject jo = items.get(position).getAsJsonObject();
        String SeriesName = JfClient.strFromGson(jo,"SeriesName");
        String SeasonName = JfClient.strFromGson(jo,"SeasonName");
        String Name = JfClient.strFromGson(jo,"Name");
        String itemid = JfClient.strFromGson(jo,"Id");
        v.id = itemid;
        v.tvName.setText(" " + SeriesName + " " + SeasonName + " " + Name);

        if(jo.has("UserData")){
            JsonObject ujo = jo.get("UserData").getAsJsonObject();
            if(ujo.has("PlayedPercentage")){
                int pp = ujo.get("PlayedPercentage").getAsInt();
                v.tvPlayedPercentage.setProgress(pp);
                v.tvPlayedPercentage.setVisibility(View.VISIBLE);
            }
        }
        v.type = JfClient.strFromGson(jo,"Type");
        String imgUrl = "";
        if(v.type.equals("Actor")){
            String PrimaryImageTag = JfClient.strFromGson(jo,"PrimaryImageTag");
            if(!PrimaryImageTag.equals("")){
                imgUrl = JfClient.GetImgUrl(v.id,PrimaryImageTag);
            }
            Log.d(TAG, "onBindViewHolder: 图像URL" + imgUrl);
        }else{
            imgUrl = JfClient.GetImgUrl(jo);
        }

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
                listener.onClick(jo);
            }
        });
    }

    public JsonArray getData(){
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
    public void setItems(JsonArray items){
        this.items = items;
        notifyDataSetChanged();
    }

    /**
     * 添加数据
     * @param items
     */
    public void addItems(JsonArray items){
        int c = this.items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(c,items.size());
    }

    public void clearItems(){
        this.items = new JsonArray();
        notifyDataSetChanged();
    }

    //定义OnItemClickListener接口
    public interface OnItemClickListener {
        void onClick(JsonObject jo);
    }

    public OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
