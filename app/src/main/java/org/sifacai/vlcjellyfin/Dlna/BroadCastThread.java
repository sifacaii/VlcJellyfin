package org.sifacai.vlcjellyfin.Dlna;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.lzy.okgo.OkGo;

import org.sifacai.vlcjellyfin.Utils.JfClient;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

public class BroadCastThread extends Thread {
    private String TAG = "广播线程";

    public static final int TYPE_DEVICE = 1;  //发现新设备
    public static final int TYPE_NEW_DEVICE_ADDED = 2; //新加了新设备
    public static final int TYPE_DEVICE_DEL = 3;  //设备离线
    public static final int TYPE_MSG = 9;    //消息

    public static final String GroupAddress = "239.255.255.250";

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

    private Handler handler;
    private MulticastSocket mSocket;
    private ArrayList<AVTransport> avTransports;
    private boolean isStop = false;

    public BroadCastThread() {
        avTransports = new ArrayList<>();
        try {
            mSocket = new MulticastSocket(1900);
            mSocket.joinGroup(InetAddress.getByName(GroupAddress));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (mSocket != null && !isStop) {
            listenr();
        }
    }

    public void SetHandler(Handler handler) {
        this.handler = handler;
    }

    public ArrayList<AVTransport> GetAVTransportArray() {
        return avTransports;
    }

    /**
     * 发送Ｍ－ＳＥＡＲＣＨ
     */
    public void refresh() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramPacket packet = new DatagramPacket(NOTIFY_rootdevice, NOTIFY_rootdevice.length);
                try {
                    packet.setAddress(InetAddress.getByName(GroupAddress));
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

    /**
     * 监听广播
     */
    private void listenr() {
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

    public void ProgressNOTIFY(String data) throws IOException, XmlPullParserException {
        if (data.startsWith("M-SEARCH")) return;
        HashMap<String, String> dh = DlnaDevice.parseNOTIFY(data);
        if (isExitsOrByeBye(dh)) return;
        boolean isav = DlnaDevice.isMediaRenderer(dh.get("nt"));
        String location = dh.get("location");
        if (location == null) location = "";
        if (isav && !location.equals("")) {
            String xmlstr = JfClient.SendGet(location);
            if (xmlstr == null || xmlstr.equals("")) return;
            findDevice(location, xmlstr);
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
                    AVTransport av = new AVTransport();
                    av.moduleName = moduleName;
                    av.serviceId = ds.serviceId;
                    av.UDN = device.UDN;
                    av.controlURL = url + (ds.controlURL.startsWith("/") ? ds.controlURL : "/" + ds.controlURL);
                    av.eventSubURL = url + (ds.eventSubURL.startsWith("/") ? ds.eventSubURL : "/" + ds.eventSubURL);
                    av.iconurl = device.icon.size() > 0 ? url + "/" + device.icon.get(0) : "";
                    avTransports.add(av);
                    if (handler != null) {
                        Message msg = new Message();
                        msg.what = TYPE_NEW_DEVICE_ADDED;
                        handler.sendMessage(msg);
                    }
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

    /**
     * 是否已存在或byebye
     * @param dh
     * @return
     */
    public boolean isExitsOrByeBye(HashMap<String,String> dh) {
        String USN = dh.get("usn");
        String[] usns = USN.split("::");
        if (usns.length < 2) return true;
        for (AVTransport av : avTransports) {
            if (usns[0].equals(av.UDN)) {
                if(dh.get("nts")!=null && dh.get("nts").toLowerCase().indexOf("byebye") >= 0){
                    avTransports.remove(av);
                    Message msg = new Message();
                    msg.what = TYPE_DEVICE_DEL;
                    handler.sendMessage(msg);
                }
                return true;
            }
        }
        return false;
    }

    public void Stop() {
        isStop = true;
    }
}
