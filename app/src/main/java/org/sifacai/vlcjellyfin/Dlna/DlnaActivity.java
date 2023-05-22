package org.sifacai.vlcjellyfin.Dlna;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;

import org.sifacai.vlcjellyfin.Component.JRecyclerView;
import org.sifacai.vlcjellyfin.R;
import org.sifacai.vlcjellyfin.Ui.BaseActivity;
import org.sifacai.vlcjellyfin.Utils.JfClient;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

public class DlnaActivity extends BaseActivity {
    private String TAG = "Dlna播放器";

    private JRecyclerView rv;
    private AVTransportAdapter avTransportAdapter;

    BroadCastThread broadCastTheader = new BroadCastThread();

    private Handler handler= new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BroadCastThread.TYPE_DEVICE:
                    break;
                case BroadCastThread.TYPE_NEW_DEVICE_ADDED:
                    avTransportAdapter.notifyDataSetChanged();
                    break;
                case BroadCastThread.TYPE_DEVICE_DEL:
                    avTransportAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dlna);
        getSupportActionBar().hide();

        broadCastTheader.SetHandler(handler);
        broadCastTheader.start();

        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                broadCastTheader.refresh();
            }
        });

        rv = findViewById(R.id.mDeviceGridView);
        V7LinearLayoutManager layoutManager = new V7LinearLayoutManager(rv.getContext());
        layoutManager.setOrientation(V7LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(layoutManager);
        avTransportAdapter = new AVTransportAdapter(this, broadCastTheader.GetAVTransportArray());
        rv.setAdapter(avTransportAdapter);
        avTransportAdapter.setOnItemClickListener(new AVTransportAdapter.OnItemClickListener() {
            @Override
            public void onClick(AVTransport avTransport) {
                //Intent intent = new Intent(DlnaActivity.this, DlnaControllActivity.class);
                //intent.putExtra("AVT",avTransport);
                //startActivity(intent);
                String vurl = JfClient.playList.get(JfClient.playIndex).Url;
                Controller.SetAVTransportURI(avTransport.controlURL, vurl, new JfClient.JJCallBack() {
                    @Override
                    public void onSuccess(String str) {
                        Controller.GetMediaInfo(avTransport.controlURL, new JfClient.JJCallBack() {
                            @Override
                            public void onSuccess(String str) {
                                Controller.Play(avTransport.controlURL, new JfClient.JJCallBack() {
                                    @Override
                                    public void onSuccess(String str) {
                                        ShowToask("已发送！");
                                    }

                                    @Override
                                    public void onError(String str) {
                                        ShowToask(str);
                                    }
                                });
                            }

                            @Override
                            public void onError(String str) {
                                Log.d(TAG, "onError: GetMediaInfo:" + str);
                            }
                        });
                        //ShowToask(str);
                    }

                    @Override
                    public void onError(String str) {
                        Log.d(TAG, "onError: SetAVTransportURI" + str);
                        ShowToask(str);
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        broadCastTheader.Stop();
    }

}