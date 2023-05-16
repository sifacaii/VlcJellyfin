package org.sifacai.vlcjellyfin.Dlna;

import java.util.ArrayList;

public class DlnaDevice {
    public String friendlyName;
    public String modelName;
    public String deviceType;
    public String UDN;
    public String location;
    public ArrayList<String> icon = new ArrayList<>();
    public ArrayList<DlnaService> DlnaServices = new ArrayList<>();
}
