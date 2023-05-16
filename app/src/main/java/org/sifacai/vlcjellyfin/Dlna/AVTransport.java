package org.sifacai.vlcjellyfin.Dlna;

import android.os.Parcel;
import android.os.Parcelable;

public class AVTransport implements Parcelable {
    public String iconurl;
    public String moduleName;
    public String UDN;
    public String controlURL;
    public String eventSubURL;
    public String serviceId;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(iconurl);
        parcel.writeString(moduleName);
        parcel.writeString(UDN);
        parcel.writeString(controlURL);
        parcel.writeString(eventSubURL);
        parcel.writeString(serviceId);
    }

    public static final Parcelable.Creator<AVTransport> CREATOR = new Parcelable.Creator<AVTransport>(){
        @Override
        public AVTransport createFromParcel(Parcel parcel) {
            AVTransport av = new AVTransport();
            av.moduleName = parcel.readString();
            av.UDN = parcel.readString();
            av.serviceId = parcel.readString();
            av.controlURL = parcel.readString();
            av.eventSubURL = parcel.readString();
            av.iconurl = parcel.readString();
            return av;
        }

        @Override
        public AVTransport[] newArray(int i) {
            return new AVTransport[i];
        }
    };
}
