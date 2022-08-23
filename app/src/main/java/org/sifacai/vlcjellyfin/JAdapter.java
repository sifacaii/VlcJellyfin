package org.sifacai.vlcjellyfin;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

        public VH(View v) {
            super(v);
            id = "";
            type = "";
            tvName = v.findViewById(R.id.tvName);
            tvCover = v.findViewById(R.id.ivThumb);
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
        String itemid = jo.get("Id").getAsString();
        v.id = itemid;
        v.tvName.setText(" " + jo.get("Name").getAsString());
        if(jo.has("CollectionType")){
            v.type = jo.get("CollectionType").getAsString();
        }
        String imgUrl = "";
        if(jo.has("ImageTags")){
            JsonObject imageTags = jo.getAsJsonObject("ImageTags");
            if(imageTags.has("Primary")){
                String picId = imageTags.get("Primary").getAsString();
                imgUrl = Utils.getImgUrl(itemid,picId);
            }
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

    //定义OnItemClickListener接口
    public interface OnItemClickListener {
        void onClick(JsonObject jo);
    }

    public OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}