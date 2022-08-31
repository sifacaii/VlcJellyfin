package org.sifacai.vlcjellyfin;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.Response;

import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class JfClient {
    public static final String TAG = "JellyfinClient";
    public static final String XEmbyAuthorization = "MediaBrowser Client=\"Vlc_J_TV\", Device=\"Vlc_J_TV\", DeviceId=\"TW96aWxsYS81LjAgKFdpbmRvd3MgTlQgNi4xOyBXa\", Version=\"10.8.1\"";
    public static HttpHeaders headers;
    public static Config config;
    public static String UserId = "";
    public static String AccessToken = "";

    public enum ReportType {
        playing,
        Progress,
        stop
    }

    /**
     * 初始化配置
     * @param application
     */
    public static void init(Application application) {
        config = new Config(application);
        SetHeaders();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(5,TimeUnit.SECONDS);
        builder.readTimeout(3, TimeUnit.SECONDS);
        builder.writeTimeout(5,TimeUnit.SECONDS);
        OkGo.getInstance().init(application)
                .setOkHttpClient(builder.build())
                .setRetryCount(3);
    }

    /**
     * 回放报告
     *
     * @param type
     * @param id
     * @param PositionTicks
     */
    public static void ReportPlayBackState(ReportType type, String id, long PositionTicks) {
        String url = config.getJellyfinUrl();
        if (type == ReportType.playing) {
            url += "/Sessions/Playing";
        } else if (type == ReportType.Progress) {
            url += "/Sessions/Playing/Progress";
        } else if (type == ReportType.stop) {
            url += "/Sessions/Playing/Stopped";
        }
        String reqstr = "{\"itemId\":\"" + id + "\",\"PositionTicks\":\"" + PositionTicks * 10000 + "\"}";
        SendPost(url, reqstr, new JJCallBack() {
            @Override
            public void onSuccess(String str) {
                //回放报告
            }
        },null);
    }

    /**
     * 获取封面图url
     *
     * @param itemid
     * @param tagid
     * @return
     */
    public static String GetImgUrl(String itemid, String tagid) {
        String url = config.getJellyfinUrl() + "/Items/" + itemid + "/Images/Primary";
        url += "?fillHeight=286&fillWidth=200&quality=96&tag=" + tagid;
        return url;
    }

    public static String GetImgUrl(JsonObject item) {
        String id = jeFromGson(item, "Id").getAsString();
        String imgid = jeFromGson(jeFromGson(item, "ImageTags").getAsJsonObject(), "Primary").getAsString();
        return GetImgUrl(id, imgid);
    }

    /**
     * 获取播放url
     *
     * @param itemid
     * @return
     */
    public static String GetPlayUrl(String itemid) {
        String playurl = config.getJellyfinUrl() + "/videos/" + itemid + "/stream.mp4?static=true&a";
        return playurl;
    }

    /**
     * 获取项目附加部分
     * @param itemid
     * @param cb
     */
    public static void GetAddPart(String itemid,JJCallBack cb,JJCallBack errcb){
        String AddPartUrl = "/Videos/" + itemid + "/AdditionalParts?userId=" + UserId;
        SendGet(AddPartUrl,new JJCallBack(){
            @Override
            public void onSuccess(String str) {
                JsonObject item = strToGson(str, JsonObject.class);
                if (null != item) {
                    JsonArray items = jeFromGson(item, "Items").getAsJsonArray();
                    cb.onSuccess(items);
                }
            }
        },errcb);
    }

    /**
     * 获取剧集
     * @param seriesId 剧ID
     * @param seasonId 季ID
     * @param cb
     */
    public static void GetEpisodes(String seriesId, String seasonId, JJCallBack cb,JJCallBack errcb) {
        String EpisodesUrl = config.getJellyfinUrl() + "/Shows/" + seriesId + "/Episodes?seasonId=" + seasonId;
        EpisodesUrl += "&userId=" + UserId;
        EpisodesUrl += "&Fields=ItemCounts,PrimaryImageAspectRatio,BasicSyncInfo,CanDelete,MediaSourceCount,Overview";

        SendGet(EpisodesUrl, new JJCallBack() {
            @Override
            public void onSuccess(String str) {
                JsonObject item = strToGson(str, JsonObject.class);
                if (null != item) {
                    JsonArray items = jeFromGson(item, "Items").getAsJsonArray();
                    cb.onSuccess(items);
                }
            }
        },errcb);

    }

    /**
     * 获取季(Seasons)数据
     *
     * @param seriesId 剧ID
     * @param cb
     */
    public static void GetSeasons(String seriesId, JJCallBack cb,JJCallBack errcb) {
        String SeasonsUrl = config.getJellyfinUrl() + "/Shows/" + seriesId + "/Seasons?userId=" + UserId;
        SeasonsUrl += "&Fields=ItemCounts,PrimaryImageAspectRatio,BasicSyncInfo,MediaSourceCount";

        SendGet(SeasonsUrl, new JJCallBack() {
            @Override
            public void onSuccess(String str) {
                JsonObject item = strToGson(str, JsonObject.class);
                if (null != item) {
                    JsonArray items = jeFromGson(item, "Items").getAsJsonArray();
                    cb.onSuccess(items);
                }
            }
        },errcb);
    }

    /**
     * 获取合集条目
     *
     * @param parentId
     * @param type      类型 movie、tvshows……
     * @param sortBy    排序条件  评分、加入时间……
     * @param sortOrder 排序规则 升序、降序
     * @param limit     每页条数
     * @param page      页
     * @param cb
     */
    public static void GetCollection(String parentId, String type, String sortBy, String sortOrder, int limit, int page, JJCallBack cb,JJCallBack errcb) {
        String itemsUrl = config.getJellyfinUrl() + "/Users/" + UserId + "/Items?ParentId=" + parentId + "&Limit=" + limit;
        itemsUrl += "&Recursive=true&Fields=PrimaryImageAspectRatio,BasicSyncInfo,Seasons,Episodes&ImageTypeLimit=1";
        itemsUrl += "&EnableImageTypes=Primary,Backdrop,Banner,Thumb";
        itemsUrl += "&SortBy=" + sortBy + ",SortName,ProductionYear&SortOrder=" + sortOrder;

        if (type.equals("tvshows")) {
            itemsUrl += "&IncludeItemTypes=Series";
        } else if (type.equals("movies")) {
            itemsUrl += "&IncludeItemTypes=Movie";
        } else {
            itemsUrl += "&IncludeItemTypes=Movie,Series";
        }
        int startIndex = page * limit - limit;
        itemsUrl += "&StartIndex=" + startIndex;

        SendGet(itemsUrl, new JJCallBack() {
            @Override
            public void onSuccess(String str) {
                JsonObject items = strToGson(str, JsonObject.class);
                cb.onSuccess(items);
            }
        },errcb);
    }

    /**
     * 获取最新项目
     *
     * @param parentId
     * @param cb
     */
    public static void GetLatest(String parentId, JJCallBack cb,JJCallBack errcb) {
        String lastestUrl = config.getJellyfinUrl() + "/Users/" + UserId + "/Items/Latest?";
        lastestUrl += "Limit=16&Fields=PrimaryImageAspectRatio%2CBasicSyncInfo%2CPath";
        lastestUrl += "&ImageTypeLimit=1&EnableImageTypes=Primary,Backdrop,Thumb";
        lastestUrl += "&ParentId=" + parentId;

        SendGet(lastestUrl, new JJCallBack() {
            @Override
            public void onSuccess(String str) {
                JsonArray latestObj = strToGson(str, JsonArray.class);
                if (null != latestObj) {
                    cb.onSuccess(latestObj);
                }
            }
        },errcb);
    }

    /**
     * 获取最近播放
     *
     * @param cb
     */
    public static void GetResume(JJCallBack cb,JJCallBack err) {
        String resumeUrl = config.getJellyfinUrl() + "/Users/" + UserId + "/Items/Resume?";
        resumeUrl += "Limit=12&Recursive=true&Fields=PrimaryImageAspectRatio,BasicSyncInfo";
        resumeUrl += "&ImageTypeLimit=1&EnableImageTypes=Primary,Backdrop,Thumb";
        resumeUrl += "&EnableTotalRecordCount=false&MediaTypes=Video";

        SendGet(resumeUrl, new JJCallBack() {
            @Override
            public void onSuccess(String str) {
                JsonObject resumeObj = strToGson(str, JsonObject.class);
                if (null != resumeObj) {
                    JsonArray resumes = jeFromGson(resumeObj, "Items").getAsJsonArray();
                    cb.onSuccess(resumes);
                }
            }
        },err);
    }

    /**
     * 获取首页
     *
     * @param cb
     */
    public static void GetViews(JJCallBack cb,JJCallBack err) {
        String viewsUrl = config.getJellyfinUrl() + "/Users/" + UserId + "/Views";
        SendGet(viewsUrl, new JJCallBack() {
            @Override
            public void onSuccess(String str) {
                JsonObject viewsObj = strToGson(str, JsonObject.class);
                if (null != viewsObj) {
                    JsonArray views = jeFromGson(viewsObj, "Items").getAsJsonArray();
                    cb.onSuccess(views);
                }
            }
        },err);
    }

    /**
     * 获取项目详情
     *
     * @param itemid
     * @param cb
     */
    public static void GetItemInfo(String itemid, JJCallBack cb,JJCallBack err) {
        String url = config.getJellyfinUrl() + "/Users/" + UserId + "/Items/" + itemid;
        SendGet(url, new JJCallBack() {
            @Override
            public void onSuccess(String str) {
                JsonObject item = strToGson(str, JsonObject.class);
                if (null != item) {
                    cb.onSuccess(item);
                }
            }
        },err);
    }

    /**
     * 验证用户名密码
     *
     * @param username
     * @param password
     * @param cb
     */
    public static void AuthenticateByName(String username, String password, JJCallBack cb,JJCallBack err, boolean saveUser) {
        String url = config.getJellyfinUrl() + "/Users/authenticatebyname";
        String reqjson = "{\"Username\":\"" + username + "\",\"Pw\":\"" + password + "\"}";
        SendPost(url, reqjson, new JJCallBack() {
            @Override
            public void onSuccess(String str) {
                JsonObject userObj = strToGson(str, JsonObject.class);
                if (userObj != null) {
                    String userId = jeFromGson(jeFromGson(userObj, "User").getAsJsonObject(), "Id").getAsString();
                    String Token = jeFromGson(userObj, "AccessToken").getAsString();
                    if (!Token.equals("")) {
                        UserId = userId;
                        AccessToken = Token;
                        if (saveUser) {
                            config.setUserName(username);
                            config.setPassWord(password);
                        }
                        SetHeaders();
                        cb.onSuccess(true);
                    } else {
                        cb.onSuccess(false);
                    }
                } else {
                    cb.onSuccess(false);
                }
            }
        },err);
    }

    /**
     * 获取用户列表
     *
     * @param cb
     */
    public static void GetUsers(JJCallBack cb,JJCallBack err) {
        String url = config.getJellyfinUrl() + "/users/public";
        SendGet(url, new JJCallBack() {
            @Override
            public void onSuccess(String str) {
                JsonArray users = strToGson(str, JsonArray.class);
                cb.onSuccess(users);
            }
        },err);
    }

    /**
     * 验证服务器地址
     *
     * @param url
     * @param cb
     */
    public static void VerityServerUrl(String url, JJCallBack cb,JJCallBack err) {
        if (url.length() > 0) {
            if (url.startsWith("http://") || url.startsWith("https://")) {

                SendGet(url + "/system/info/public", new JJCallBack() {
                    @Override
                    public void onSuccess(String str) {
                        JsonObject serverInfo = strToGson(str, JsonObject.class);
                        Log.d(TAG, "onSuccess: " + str);
                        String ServerId = jeFromGson(serverInfo, "Id") == null ? null : jeFromGson(serverInfo, "Id").getAsString();
                        if (ServerId == null || ServerId.length() == 0) {
                            cb.onSuccess(false);
                        } else {
                            config.setJellyfinUrl(url);
                            cb.onSuccess(true);
                        }
                    }
                },err);

            }
        }else{
            cb.onSuccess(false);
        }
    }

    /**
     * 设置请求头
     */
    public static void SetHeaders() {
        headers = new HttpHeaders();
        String xea = XEmbyAuthorization;
        if (null != AccessToken && AccessToken.length() > 0) {
            xea += ", Token=\"" + AccessToken + "\"";
        }
        headers.put("Accept", "application/json");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        headers.put("X-Emby-Authorization", xea);
    }

    /**
     * Get
     *
     * @param url
     * @param cb
     */
    public static void SendGet(String url, JJCallBack cb,JJCallBack errcb) {
            OkGo.<String>get(url).headers(headers).execute(new AbsCallback<String>() {
                @Override
                public String convertResponse(okhttp3.Response response) throws IOException {
                    String result = "";
                    if (null != response.body()) {
                        result = response.body().string();
                    }
                    return result;
                }

                @Override
                public void onSuccess(Response<String> response) {
                    cb.onSuccess(response.body());
                }

                @Override
                public void onError(Response<String> response) {
                    if(errcb != null){
                        errcb.onError(response.body());
                    }
                }
            });
    }

    /**
     * Post
     *
     * @param url
     * @param jsonStr
     * @param cb
     */
    public static void SendPost(String url, String jsonStr, JJCallBack cb,JJCallBack errcb) {
        OkGo.<String>post(url).headers(headers).upJson(jsonStr).execute(new AbsCallback<String>() {
            @Override
            public String convertResponse(okhttp3.Response response) throws Throwable {
                String result = "";
                if (null != response.body()) {
                    result = response.body().string();
                }
                return result;
            }

            @Override
            public void onSuccess(Response<String> response) {
                cb.onSuccess(response.body());
            }

            @Override
            public void onError(Response<String> response) {
                if(errcb != null){
                    errcb.onError(response.body());
                }
            }
        });
    }

    /**
     * Json字符串转Gson对象
     *
     * @param jsonStr
     * @param tClass
     * @param <T>
     * @return 可能是NULL
     */
    public static <T> T strToGson(String jsonStr, Class<T> tClass) {
        if (jsonStr != null && jsonStr.length() > 0) {
            try {
                return new Gson().fromJson(jsonStr, tClass);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static String strFromGson(JsonObject obj,String key){
        JsonElement jo = jeFromGson(obj,key);
        if(jo == null){
            return "";
        }else{
            return jo.getAsString();
        }
    }

    /**
     * 按key获取JsonElement
     *
     * @param obj
     * @param key
     * @return
     */
    public static JsonElement jeFromGson(JsonObject obj, String key) {
        JsonElement jo = null;
        if (obj != null && obj.has(key)) {
            jo = obj.get(key);
        }
        return jo;
    }

    public static class JJCallBack implements JCallBack {

        @Override
        public void onSuccess(String str) {

        }

        @Override
        public void onSuccess(Boolean bool) {

        }

        @Override
        public void onSuccess(JsonObject jsonObject) {

        }

        @Override
        public void onSuccess(JsonArray jsonArray) {

        }

        @Override
        public void onError(String str) {

        }
    }

    public interface JCallBack {
        void onSuccess(String str);

        void onSuccess(Boolean bool);

        void onSuccess(JsonObject jsonObject);

        void onSuccess(JsonArray jsonArray);

        void onError(String str);
    }
}
