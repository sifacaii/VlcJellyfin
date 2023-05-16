package org.sifacai.vlcjellyfin.Dlna;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.util.Xml;
import android.view.View;

import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;

import org.sifacai.vlcjellyfin.Component.JRecyclerView;
import org.sifacai.vlcjellyfin.R;
import org.sifacai.vlcjellyfin.Ui.BaseActivity;
import org.sifacai.vlcjellyfin.Utils.JfClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;

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
                    Log.d(TAG, "handleMessage: " + avt);
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
                Intent intent = new Intent(DlnaActivity.this, DlnaControllActivity.class);
                intent.putExtra("AVT",avTransport);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        listen_thread.start();
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
            byte[] buff = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buff, buff.length);
            while (mSocket != null) {
                try {
                    mSocket.receive(packet);
                    String clientIP = packet.getAddress().getHostAddress();
                    int clientPort = packet.getPort();
                    String data = new String(packet.getData()).trim();
                    Log.d(TAG, "listen: " + clientIP + ":" + clientPort + "：" + data);
                    ProgressNOTIFY(data);
                } catch (IOException | XmlPullParserException e) {
                    throw new RuntimeException(e);
                }
            }
            handler.post(listen_thread);
        }
    };

    public void ProgressNOTIFY(String data) throws IOException, XmlPullParserException {
        String[] notify = data.split("\n");
        boolean isav = false;
        String location = "";
        for (String n : notify) {
            String[] ns = n.split(":", 2);
            //Log.d(TAG, "ProgressNOTIFY: " + String.join(",",ns));
            if (ns.length < 2) continue;
            else if (ns[0].equals("Location")) location = ns[1];
            else if (ns[0].equals("ST")) {
                String nsnt = ns[1].trim();
                if (nsnt.equals("upnp:rootdevice") || nsnt.indexOf("device:MediaRenderer") >= 0) {
                    isav = true;
                }
            }
        }
        if (isav && !location.equals("")) {
            Log.d(TAG, "ProgressNOTIFY: " + location);
            String finalLocation = location;
            JfClient.SendGet(location,new JfClient.JJCallBack(){
                @Override
                public void onSuccess(String str) {
                    Log.d(TAG, "onSuccess: " + str);
                    findDevice(finalLocation,str);
                }
            },new JfClient.JJCallBack(){
                @Override
                public void onError(String str) {
                    ShowToask(str);
                }
            });
        }
    }

    public void findDevice(String location,String xml){
        DlnaDevice device;
        try {
            device = ParseXML(xml);
            for (int i = 0; i < device.DlnaServices.size(); i++) {
                DlnaService ds = device.DlnaServices.get(i);
                if (ds.serviceType.indexOf("service:AVTransport") > -1) {
                    int si = location.indexOf("/", 8);
                    String url = si > -1 ? location.substring(0, si) : location;
                    String moduleName = device.friendlyName.equals("") ? device.modelName : device.friendlyName;
                    Bundle bundle = new Bundle();
                    bundle.putString("moduleName",moduleName);
                    bundle.putString("UDN", device.UDN);
                    bundle.putString("serviceId",ds.serviceId);
                    bundle.putString("controlURL",url + "/" + ds.controlURL);
                    bundle.putString("eventSubURL",url + "/" + ds.eventSubURL);
                    bundle.putString("iconurl",device.icon.size() > 0 ? url + "/" + device.icon.get(0) : "");
                    Message msg = new Message();
                    msg.what = 1;
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            }
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DlnaDevice ParseXML(String xml) throws XmlPullParserException, IOException {
        Log.d(TAG, "ParseXML: " + xml);
        XmlPullParser xmlPullParser = Xml.newPullParser();
        xmlPullParser.setInput(new StringReader(xml));

        DlnaDevice device = new DlnaDevice();

        int eventType = xmlPullParser.getEventType();
        String tagName = "";
        DlnaService service = null;
        String icon = "";
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    tagName = xmlPullParser.getName().toLowerCase();
                    if (tagName.equals("service")) service = new DlnaService();
                    if (tagName.equals("icon")) icon = "";
                    break;
                case XmlPullParser.TEXT:
                    String value = xmlPullParser.getText();
                    value = value == null ? "" : value.trim();
                    if (tagName.equals("friendlyname")) device.friendlyName = value;
                    if (tagName.equals("devicetype")) device.deviceType = value;
                    if (tagName.equals("modelname")) device.modelName = value;
                    if (tagName.equals("udn")) device.UDN = value;

                    if (tagName.equals("url")) icon = value;

                    if (tagName.equals("servicetype")) service.serviceType = value;
                    if (tagName.equals("serviceid")) service.serviceId = value;
                    if (tagName.equals("controlurl")) service.controlURL = value;
                    if (tagName.equals("scpdurl")) service.SCPDURL = value;
                    if (tagName.equals("eventsuburl")) service.eventSubURL = value;
                    break;
                case XmlPullParser.END_TAG:
                    if (xmlPullParser.getName().toLowerCase().equals("service")) device.DlnaServices.add(service);
                    if (xmlPullParser.getName().toLowerCase().equals("icon")) device.icon.add(icon);
                    break;
            }
            eventType = xmlPullParser.next();
        }
        return device;
    }

    private String getRspXML(String action, HashMap<String, String> map) {
        String rsp = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"" +
                " s:encodingstyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
                "<s:Body>" +
                "<u:" + action + "Response xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">";

        if (map != null) {
            for (String key : map.keySet()) {
                rsp += "<" + key + ">" + map.get(key) + "</" + key + ">";
            }
        }

        rsp += "</u:" + action + "Response>" +
                "</s:Body>" +
                "</s:Envelope>";

        return rsp;
    }
}