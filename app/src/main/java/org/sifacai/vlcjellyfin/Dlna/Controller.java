package org.sifacai.vlcjellyfin.Dlna;

import android.util.Log;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.callback.Callback;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.Response;

import org.sifacai.vlcjellyfin.Utils.JfClient;

import java.io.IOException;

public class Controller {

    public static String TAG = "ＤＬＮＡ控制";

    public static void SetAVTransportURI(String controlUrl, String url, JfClient.JJCallBack cb) {
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<s:Body>" +
                "<u:SetAVTransportURI xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">" +
                "<InstanceID>0</InstanceID>" +
                "<CurrentURI><![CDATA[" + url + "]]></CurrentURI>" +
                "<CurrentURIMetaData></CurrentURIMetaData>" +
                "</u:SetAVTransportURI>" +
                "</s:Body></s:Envelope>";
        HttpHeaders headers = new HttpHeaders();
        headers.put("SOAPACTION", "\"urn:schemas-upnp-org:service:AVTransport:1#SetAVTransportURI\"");
        PostXML(controlUrl, xml, headers, cb);
    }

    public static void GetMediaInfo(String controlUrl, JfClient.JJCallBack cb) {
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<s:Body>" +
                "<u:GetMediaInfo xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">" +
                "<InstanceID>0</InstanceID>" +
                "</u:GetMediaInfo>" +
                "</s:Body></s:Envelope>";
        HttpHeaders headers = new HttpHeaders();
        headers.put("SOAPACTION", "\"urn:schemas-upnp-org:service:AVTransport:1#GetMediaInfo\"");
        PostXML(controlUrl, xml, headers, cb);
    }

    public static void Play(String controlUrl, JfClient.JJCallBack cb) {
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<s:Body>" +
                "<u:Play xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">" +
                "<InstanceID>0</InstanceID>" +
                "<Speed>1</Speed>" +
                "</u:Play>" +
                "</s:Body></s:Envelope>";
        HttpHeaders headers = new HttpHeaders();
        headers.put("SOAPACTION", "\"urn:schemas-upnp-org:service:AVTransport:1#Play\"");
        PostXML(controlUrl, xml, headers, cb);
    }

    public static void PostXML(String url, String xml, HttpHeaders headers, JfClient.JJCallBack callBack) {
        OkGo.<String>post(url)
                .upString(xml)
                .headers(headers)
                .headers("Content-Type", "text/xml; encoding=\"utf-8\"")
                .execute(new AbsCallback<String>() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if (callBack != null) {
                            callBack.onSuccess(response.body());
                        }
                    }

                    @Override
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        String result = "";
                        if (null != response.body()) {
                            result = response.body().string();
                        }
                        return result;
                    }

                    @Override
                    public void onError(Response<String> response) {
                        if (callBack != null) callBack.onError(response.message());
                    }
                });

    }
}
