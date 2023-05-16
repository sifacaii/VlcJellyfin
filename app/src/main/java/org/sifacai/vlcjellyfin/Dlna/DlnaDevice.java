package org.sifacai.vlcjellyfin.Dlna;

import java.util.ArrayList;
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
        if(nt.equals("upnp:rootdevice")) return  true;
        if(nt.indexOf("device:MediaRenderer") >= 0) return true;
        return false;
    }
}
