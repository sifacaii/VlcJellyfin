package org.sifacai.vlcjellyfin.Dlna;

import android.util.Log;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.callback.Callback;
import com.lzy.okgo.model.Response;

import java.io.IOException;

public class Controller {

    public static String TAG = "ＤＬＮＡ控制";

    public static void SetAVTransportURI(String controlUrl,String url){
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<s:Body>" +
                "<u:SetAVTransportURI xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">" +
                "<InstanceID>0</InstanceID>" +
                "<CurrentURI><![CDATA["+url+"]]></CurrentURI>" +
                "<CurrentURIMetaData></CurrentURIMetaData>" +
                "</u:SetAVTransportURI>" +
                "</s:Body></s:Envelope>";
        PostXML(controlUrl,xml);
    }

    public static void PostXML(String url,String xml){

        OkGo.<String>post(url)
                .upString(xml)
                .headers("Content-Type","text/xml")
                .headers("charset","utf-8")
                .execute(new AbsCallback<String>() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.d(TAG, "onSuccess: " + response.body());
                    }

                    @Override
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        Log.d(TAG, "convertResponse: " + response.message());
                        return null;
                    }
                });

    }
}
