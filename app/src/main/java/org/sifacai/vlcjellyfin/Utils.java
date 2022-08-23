package org.sifacai.vlcjellyfin;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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

    public static int playIndex = 0; //当前播放
    public static ArrayList<Video> playList = new ArrayList<>(); //播放列表

    /**
     * GET请求
     * @param url
     * @return
     */
    public static String okhttpSend(String url){
        return okhttpSend(url,null);
    }

    /**
     * POST JSON数据
     * @param url
     * @param jsonStr
     * @return
     */
    public static String okhttpSend(String url, String jsonStr){
        if(url.startsWith("http")){

        }else{
            url = JellyfinUrl + url;
        }
        OkHttpClient client = new OkHttpClient();

        String xea = XEmbyAuthorization;
        if(AccessToken != ""){
            xea += ", Token=\"" + AccessToken + "\"";
        }
        Headers headers = new Headers.Builder()
                .add("Accept", "application/json")
                .add("Accept-Language", "zh-CN,zh;q=0.9")
                .add("X-Emby-Authorization", xea)
                .build();

        Request request = null;
        if(jsonStr != null) {
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonStr);
            request = new Request.Builder()
                    .url(url)
                    .headers(headers)
                    .post(body)
                    .build();
        }else{
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
     * @return
     */
    public static String getImgUrl(String itemid, String tagid) {
        String url = JellyfinUrl + "/Items/" + itemid + "/Images/Primary";
        url += "?fillHeight=286&fillWidth=200&quality=96&tag=" + tagid;
        return url;
    }

    /**
     * 获取Json项
     * @param jo
     * @param key
     * @return
     */
    public static JsonElement getJsonString(JsonObject jo,String key){
        JsonElement je = new Gson().toJsonTree("",String.class);
        if(jo.has(key)){
            je = jo.get(key);
        }
        return je;
    }

    public static int getPixelsFromDp(Activity context, int i){
        DisplayMetrics metrics =new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return (i * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;
    }

    /**
     * 报告播放进度
     * @param baseUrl
     * @param Id
     * @param paused
     * @param PositionTicks
     * @param token
     */
    public static void ReportPlaybackProgress(String baseUrl, String Id, boolean paused, long PositionTicks,String token) {
        String json = "{\"itemId\" : \"" + Id + "\",\"canSeek\" : \"true\",\"isPaused\":\"" + paused + "\",\"isMuted\":\"false\",";
        json += "\"positionTicks\": \"" + PositionTicks * 10000 + "\",\"PlayMethod\":\"DirectStream\"}";
        String url = baseUrl + "/Sessions/Playing/Progress";
        okhttpSend(url,json);
    }

    /**
     * 播放停止
     * @param baseUrl
     * @param Id
     * @param PositionTicks
     * @param token
     */
    public static void ReportPlaybackStop(String baseUrl, String Id, long PositionTicks,String token) {
        String url = baseUrl + "/Sessions/Playing/Stopped";
        String json = "{\"itemId\":\"" + Id + "\",\"PositionTicks\":\"" + PositionTicks * 10000 + "\"}";
        okhttpSend(url,json);
    }

    public static int dp2px(Context context, float value) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics()) + 0.5f);
    }

    public static int sp2px(Context context, float value) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, context.getResources().getDisplayMetrics()) + 0.5f);
    }

    public static int pt2px(Context context, float value) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, value, context.getResources().getDisplayMetrics()) + 0.5f);
    }

    public static int in2px(Context context, float value) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, value, context.getResources().getDisplayMetrics()) + 0.5f);
    }

    public static int mm2px(Context context, float value) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, value, context.getResources().getDisplayMetrics()) + 0.5f);
    }
}
