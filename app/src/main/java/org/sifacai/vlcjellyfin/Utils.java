package org.sifacai.vlcjellyfin;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.callback.Callback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.HttpHeaders;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Utils {

    /**
     * 标准时间转换
     * @param utcTime
     * @return
     */
    public static String UtcToLocal(String utcTime){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dt = "";
        try {
            dt = df.parse(utcTime).toLocaleString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt;
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

}
