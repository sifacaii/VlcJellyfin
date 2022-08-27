package org.sifacai.vlcjellyfin;

import static android.net.sip.SipErrorCode.TIME_OUT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Utils {
    public static final String XEmbyAuthorization = "MediaBrowser Client=\"CatTv\", Device=\"CatTv\", DeviceId=\"TW96aWxsYS81LjAgKFdpbmRvd3MgTlQgNi4xOyBXa\", Version=\"10.8.1\"";
    public static String JellyfinUrl = "";
    public static String UserName = "";
    public static String PassWord = "";
    public static String UserId = "";
    public static String AccessToken = "";
    public static String SortBy = "DateCreated";
    public static String SortOrder = "Descending";

    public static int playIndex = 0; //当前播放
    public static ArrayList<Video> playList = new ArrayList<>(); //播放列表


    /**
     * GET请求
     *
     * @param url
     * @return
     */
    public static String okhttpSend(String url) {
        return okhttpSend(url, null);
    }

    /**
     * POST JSON数据
     *
     * @param url
     * @param jsonStr
     * @return
     */
    public static String okhttpSend(String url, String jsonStr) {
        if (url.startsWith("http")) {

        } else {
            url = JellyfinUrl + url;
        }
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient client = builder.sslSocketFactory(RxUtils.createSSLSocketFactory())
                .hostnameVerifier(new RxUtils.TrustAllHostnameVerifier())
                .retryOnConnectionFailure(true).build();
        //OkHttpClient client = new OkHttpClient();


        String xea = XEmbyAuthorization;
        if (AccessToken != "") {
            xea += ", Token=\"" + AccessToken + "\"";
        }
        Headers headers = new Headers.Builder()
                .add("Accept", "application/json")
                .add("Accept-Language", "zh-CN,zh;q=0.9")
                .add("X-Emby-Authorization", xea)
                .build();

        Request request = null;
        if (jsonStr != null) {
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonStr);
            request = new Request.Builder()
                    .url(url)
                    .headers(headers)
                    .post(body)
                    .build();
        } else {
            request = new Request.Builder().url(url).headers(headers).build();
        }
        Response response = null;

        try {
            response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取条目图片URL
     *
     * @return
     */
    public static String getImgUrl(String itemid, String tagid) {
        String url = JellyfinUrl + "/Items/" + itemid + "/Images/Primary";
        url += "?fillHeight=286&fillWidth=200&quality=96&tag=" + tagid;
        return url;
    }

    public static <T> T JsonToObj(String jsonStr, Class<T> tClass) {
        if (jsonStr != null && jsonStr.length() > 0) {
            try {
                return new Gson().fromJson(jsonStr, tClass);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取Json项
     *
     * @param jo
     * @param key
     * @return
     */
    public static JsonElement getJsonString(JsonObject jo, String key) {
        JsonElement je = new Gson().toJsonTree("", String.class);
        if (jo.has(key)) {
            je = jo.get(key);
        }
        return je;
    }

    public static int getPixelsFromDp(Activity context, int i) {
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return (i * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;
    }

    public enum ReportType {
        playing,
        Progress,
        stop
    }

    /**
     * 报告播放开始
     *
     * @param PositionTicks
     */
    public static void ReportPlaying(String Id, long PositionTicks) {
        String url = JellyfinUrl + "/Sessions/Playing";
        String json = "{\"itemId\":\"" + Id + "\",\"PositionTicks\":\"" + PositionTicks * 10000 + "\"}";
        String rsp = okhttpSend(url, json);
        //Log.d("VLC播放器", "ReportPlaying: " + Id + " : " + rsp);
    }

    /**
     * 报告播放进度
     *
     * @param PositionTicks
     */
    public static void ReportPlaybackProgress(String Id, long PositionTicks) {
        String json = "{\"itemId\" : \"" + Id + "\",\"positionTicks\": \"" + PositionTicks * 10000 + "\"}";
        //JsonObject rjo = new JsonObject();
        //rjo.addProperty("itemId",Id);
        //rjo.addProperty("canSeek",true);
        //rjo.addProperty("isPaused",paused);
        //rjo.addProperty("isMuted",false);
        //rjo.addProperty("positionTicks",PositionTicks * 10000);
        //rjo.addProperty("PlayMethod","DirectPlay");
        //String json = rjo.toString();
        String url = JellyfinUrl + "/Sessions/Playing/Progress";
        String rsp = okhttpSend(url, json);
        //Log.d("VLC播放器", "ReportPlaybackProgress: 返回：" + Id + ":" + rsp);
    }

    /**
     * 播放停止
     *
     * @param PositionTicks
     */
    public static void ReportPlaybackStop(String Id, long PositionTicks) {
        String url = JellyfinUrl + "/Sessions/Playing/Stopped";
        String json = "{\"itemId\":\"" + Id + "\",\"PositionTicks\":\"" + PositionTicks * 10000 + "\"}";
        String rsp = okhttpSend(url, json);
        //Log.d("VLC播放器", "ReportPlaybackStop: " + Id + " : " + rsp);
    }

    /**
     * 根据缩放类型取名称
     *
     * @param scaleName
     * @return
     */
    public static String getVlcScaleTypeName(String scaleName) {
        switch (scaleName) {
            case "SURFACE_BEST_FIT":
                return "自动";
            case "SURFACE_FIT_SCREEN":
                return "适应屏幕";
            case "SURFACE_FILL":
                return "满屏";
            case "SURFACE_16_9":
                return "16:9";
            case "SURFACE_4_3":
                return "4:3";
            case "SURFACE_ORIGINAL":
                return "原始";
        }
        return "";
    }

    public enum SortByType {
        评分("CommunityRating"),
        加入日期("DateCreated"),
        播放日期("DatePlayed"),
        家长分级("OfficialRating"),
        播放次数("PlayCount"),
        发行日期("PremiereDate"),
        播放时长("Runtime");

        public String value;

        SortByType(String value) {
            this.value = value;
        }
    }

    public enum SotrOrderType{
        升序("Ascending"),
        降序("Descending");

        public String value;

        SotrOrderType(String value) {
            this.value = value;
        }
    }
}
