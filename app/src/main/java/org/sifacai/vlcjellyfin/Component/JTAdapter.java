package org.sifacai.vlcjellyfin.Component;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.sifacai.vlcjellyfin.Bean.Item;
import org.sifacai.vlcjellyfin.R;

import java.util.List;

import me.jessyan.autosize.utils.AutoSizeUtils;

public class JTAdapter extends RecyclerView.Adapter {

    private String TAG = "JAdapter:";
    private List<Item> items;
    public JAdapter.OnItemClickListener listener;

    public JTAdapter(List<Item> items) {
        this.items = items;
    }

    public void setOnItemClickListener(JAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context c = parent.getContext();
        TextView tv = new TextView(c);
        // tv.setLayoutParams(new ViewGroup.LayoutParams(R.dimen.vertical_cover_width, R.dimen.title_height));
        tv.setWidth(AutoSizeUtils.dp2px(c, 270));
        tv.setHeight(AutoSizeUtils.dp2px(c, 32));
        tv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        //tv.setMarqueeRepeatLimit();
        tv.setPadding(0, 0, 0, 0);
        tv.setGravity(Gravity.CENTER);
        tv.setSingleLine(true);
        tv.setFocusable(true);
        tv.setBackgroundResource(R.drawable.shape_user_focus_vholder);
        return new RecyclerView.ViewHolder(tv) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item it = items.get(position);
        String path = it.getPath();
        if (path != null) {
            int pos = path.lastIndexOf("/");
            if (pos >= 0) path = path.substring(pos + 1);
            int ppos = path.lastIndexOf(".");
            if (ppos >= 0) path = path.substring(0, ppos);
        } else {
            path = "第 " + position + " 集";
        }
        String filename = items.get(position).getName();
        TextView tv = (TextView) holder.itemView;
        tv.setText(path);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClick(it);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<Item> getData() {
        return items;
    }
}
