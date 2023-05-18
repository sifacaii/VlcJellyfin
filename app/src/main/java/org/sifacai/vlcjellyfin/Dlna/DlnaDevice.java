package org.sifacai.vlcjellyfin.Dlna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DlnaDevice {
    public String friendlyName;
    public String modelName;
    public String deviceType;
    public String UDN;
    public String location;
    public ArrayList<String> icon = new ArrayList<>();
    public ArrayList<DlnaService> DlnaServices = new ArrayList<>();

    public String[] MediaRendererDesc = {"upnp:rootdevice","device:MediaRenderer"};
    public static boolean isMediaRenderer(String nt){
        if(nt == null) return false;
        if(nt.equals("upnp:rootdevice")) return  true;
        if(nt.indexOf("device:MediaRenderer") >= 0) return true;
        if(nt.indexOf("service:AVTransport") >= 0) return true;
        return false;
    }

    public static HashMap<String,String> parseNOTIFY(String data){
        HashMap<String,String> hm = new HashMap<>();
        String[] notify = data.split("\n");
        for (String n:notify) {
            String[] ns = n.split(":", 2);
            if (ns.length < 2) continue;
            hm.put(ns[0].toLowerCase(),ns[1].trim());
        }
        return hm;
    }
}
