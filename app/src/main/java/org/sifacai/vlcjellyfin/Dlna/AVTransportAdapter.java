package org.sifacai.vlcjellyfin.Dlna;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AVTransportAdapter extends RecyclerView.Adapter{
    private Context context;
    private ArrayList<AVTransport> avTransports;

    public AVTransportAdapter(Context context) {
        this.context = context;
        this.avTransports = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View ll = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, parent, false);
        return new RecyclerView.ViewHolder(ll) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        TextView tv = holder.itemView.findViewById(android.R.id.text1);
        tv.setText(avTransports.get(position).moduleName);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null){
                    listener.onClick(avTransports.get(position));
                }
            }
        });
    }

    public void addDevice(AVTransport avTransport) {
        boolean isexits = false;
        for(AVTransport av : avTransports){
            if(av.UDN.equals(avTransport.UDN)) isexits = true;
        }
        if(!isexits) {
            avTransports.add(avTransport);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return avTransports.size();
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    public interface OnItemClickListener{
        void onClick(AVTransport avTransport);
    }
}
