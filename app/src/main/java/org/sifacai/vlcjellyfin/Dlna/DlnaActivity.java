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
    private MulticastSocket mSocket;
    private Thread listen_thread;

    private byte[] NOTIFY_rootdevice = ("M-SEARCH * HTTP/1.1\n" +
            "ST: upnp:rootdevice\n" +
            "MX: 10\n" +
            "MAN: \"ssdp:discover\"\n" +
            "Content-Length: 0\n" +
            "HOST: 239.255.255.250:1900").getBytes();

    private byte[] NOTIFY_MediaRenderer = ("M-SEARCH * HTTP/1.1\n" +
            "ST: urn:schemas-upnp-org:device:MediaRenderer:1\n" +
            "MX: 10\n" +
            "MAN: \"ssdp:discover\"\n" +
            "Content-Length: 0\n" +
            "HOST: 239.255.255.250:1900").getBytes();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Bundle b = msg.getData();
                    AVTransport avt = new AVTransport();
                    avt.moduleName = b.getString("moduleName");
                    avt.serviceId = b.getString("serviceId");
                    avt.UDN = b.getString("UDN");
                    avt.controlURL = b.getString("controlURL");
                    avt.eventSubURL = b.getString("eventSubURL");
                    avt.iconurl = b.getString("iconurl");
                    avTransportAdapter.addDevice(avt);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dlna);
        getSupportActionBar().hide();

        try {
            mSocket = new MulticastSocket(1900);
            mSocket.joinGroup(InetAddress.getByName("239.255.255.250"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        listen_thread = new Thread(listen_Runnable);

        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });

        rv = findViewById(R.id.mDeviceGridView);
        V7LinearLayoutManager layoutManager = new V7LinearLayoutManager(rv.getContext());
        layoutManager.setOrientation(V7LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(layoutManager);
        avTransportAdapter = new AVTransportAdapter(this);
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
                        Controller.GetMediaInfo(avTransport.controlURL,new JfClient.JJCallBack(){
                            @Override
                            public void onSuccess(String str) {
                                Controller.Play(avTransport.controlURL,new JfClient.JJCallBack(){
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
    protected void onStart() {
        super.onStart();
        if (!listen_thread.isAlive()) listen_thread.start();
        refresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        listen_thread.interrupt();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 刷新设备
     */
    private void refresh() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramPacket packet = new DatagramPacket(NOTIFY_rootdevice, NOTIFY_rootdevice.length);
                try {
                    packet.setAddress(InetAddress.getByName("239.255.255.250"));
                    packet.setPort(1900);
                    mSocket.send(packet);
                    packet.setData(NOTIFY_MediaRenderer);
                    mSocket.send(packet);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private Runnable listen_Runnable = new Runnable() {
        @Override
        public void run() {
            while (mSocket != null) {
                try {
                    byte[] buff = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buff, buff.length);
                    mSocket.receive(packet);
                    String clientIP = packet.getAddress().getHostAddress();
                    int clientPort = packet.getPort();
                    String data = new String(packet.getData()).trim();
                    ProgressNOTIFY(data);
                } catch (IOException | XmlPullParserException e) {
                    throw new RuntimeException(e);
                }
            }
            handler.post(listen_thread);
        }
    };

    public void ProgressNOTIFY(String data) throws IOException, XmlPullParserException {
        if(data.startsWith("M-SEARCH")) return;
        HashMap<String,String> dh = DlnaDevice.parseNOTIFY(data);
        boolean isav = DlnaDevice.isMediaRenderer(dh.get("NT"));
        String location = dh.get("Location");
        if(location == null) location = "";
        if (isav && !location.equals("")) {
            String finalLocation = location;
            JfClient.SendGet(location, new JfClient.JJCallBack() {
                @Override
                public void onSuccess(String str) {
                    findDevice(finalLocation, str);
                }
            }, new JfClient.JJCallBack() {
                @Override
                public void onError(String str) {
                    ShowToask(str);
                }
            });
        }
    }

    public void findDevice(String location, String xml) {
        DlnaDevice device;
        try {
            device = XmlParser.parseX(xml);
            for (int i = 0; i < device.DlnaServices.size(); i++) {
                DlnaService ds = device.DlnaServices.get(i);
                if (ds.serviceType.indexOf("service:AVTransport") > -1) {
                    int si = location.indexOf("/", 8);
                    String url = si > -1 ? location.substring(0, si) : location;
                    String moduleName = device.friendlyName.equals("") ? device.modelName : device.friendlyName;
                    Bundle bundle = new Bundle();
                    bundle.putString("moduleName", moduleName);
                    bundle.putString("UDN", device.UDN);
                    bundle.putString("serviceId", ds.serviceId);
                    bundle.putString("controlURL", url + (ds.controlURL.startsWith("/") ? ds.controlURL : "/" + ds.controlURL));
                    bundle.putString("eventSubURL", url + "/" + ds.eventSubURL);
                    bundle.putString("iconurl", device.icon.size() > 0 ? url + "/" + device.icon.get(0) : "");
                    Message msg = new Message();
                    msg.what = 1;
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }
}