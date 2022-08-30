package org.sifacai.vlcjellyfin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.callback.Callback;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.Response;

public class JfClient {
    public final String XEmbyAuthorization = "MediaBrowser Client=\"Vlc_J_TV\", Device=\"Vlc_J_TV\", DeviceId=\"TW96aWxsYS81LjAgKFdpbmRvd3MgTlQgNi4xOyBXa\", Version=\"10.8.1\"";
    public HttpHeaders headers;
    public String UserId="";
    public String AccessToken="";
    public String serverUrl;


    /**
     * 获取封面图url
     * @param itemid
     * @param tagid
     * @return
     */
    public String GetImgUrl(String itemid, String tagid) {
        String url = serverUrl + "/Items/" + itemid + "/Images/Primary";
        url += "?fillHeight=286&fillWidth=200&quality=96&tag=" + tagid;
        return url;
    }

    public String GetImgUrl(JsonObject item) {
        String id = jeFromGson(item,"Id").getAsString();
        String imgid = jeFromGson(jeFromGson(item,"ImageTags").getAsJsonObject(),"Primary").getAsString();
        return GetImgUrl(id,imgid);
    }

    /**
     * 获取播放url
     * @param itemid
     * @return
     */
    public String GetPlayUrl(String itemid){
        String playurl = serverUrl + "/videos/" + itemid + "/stream.mp4?static=true&a";
        return playurl;
    }

    /**
     * 获取剧集
     * @param seriesId 剧ID
     * @param seasonId 季ID
     * @param cb
     */
    public void GetEpisodes(String seriesId,String seasonId,JJCallBack cb){
        String EpisodesUrl = serverUrl + "/Shows/" + seriesId + "/Episodes?seasonId=" + seasonId;
        EpisodesUrl += "&userId=" + UserId;
        EpisodesUrl += "&Fields=ItemCounts,PrimaryImageAspectRatio,BasicSyncInfo,CanDelete,MediaSourceCount,Overview";

        SendGet(EpisodesUrl,new JJCallBack(){
            @Override
            public void onSuccess(String str) {
                JsonObject item = strToGson(str,JsonObject.class);
                if (null != item) {
                    JsonArray items = jeFromGson(item,"Items").getAsJsonArray();
                    cb.onSuccess(items);
                }
            }
        });

    }

    /**
     * 获取季(Seasons)数据
     * @param seriesId 剧ID
     * @param cb
     */
    public void GetSeasons(String seriesId,JJCallBack cb){
        String SeasonsUrl = serverUrl + "/Shows/" + seriesId + "/Seasons?userId=" + UserId;
        SeasonsUrl += "&Fields=ItemCounts,PrimaryImageAspectRatio,BasicSyncInfo,MediaSourceCount";

        SendGet(SeasonsUrl,new JJCallBack(){
            @Override
            public void onSuccess(String str) {
                JsonObject item = strToGson(str,JsonObject.class);
                if(null != item){
                    JsonArray items = jeFromGson(item,"Items").getAsJsonArray();
                    cb.onSuccess(items);
                }
            }
        });
    }

    /**
     * 获取合集条目
     * @param parentId
     * @param type   类型 movie、tvshows……
     * @param sortBy  排序条件  评分、加入时间……
     * @param sortOrder 排序规则 升序、降序
     * @param limit 每页条数
     * @param page  页
     * @param cb
     */
    public void GetCollection(String parentId,String type,String sortBy,String sortOrder,int limit,int page,JJCallBack cb){
        String itemsUrl = serverUrl + "/Users/" + UserId + "/Items?ParentId=" + parentId + "&Limit=" + limit;
        itemsUrl += "&Recursive=true&Fields=PrimaryImageAspectRatio,BasicSyncInfo,Seasons,Episodes&ImageTypeLimit=1";
        itemsUrl += "&EnableImageTypes=Primary,Backdrop,Banner,Thumb";
        itemsUrl += "&SortBy="+sortBy+",SortName,ProductionYear&SortOrder=" + sortOrder;

        if (type.equals("tvshows")) {
            itemsUrl += "&IncludeItemTypes=Series";
        } else if (type.equals("movies")) {
            itemsUrl += "&IncludeItemTypes=Movie";
        } else {
            itemsUrl += "&IncludeItemTypes=Movie,Series";
        }
        int startIndex = page * limit - limit;
        itemsUrl += "&StartIndex=" + startIndex;

        SendGet(itemsUrl,new JJCallBack(){
            @Override
            public void onSuccess(String str) {
                JsonObject items = strToGson(str,JsonObject.class);
                cb.onSuccess(items);
            }
        });
    }

    /**
     * 获取最新项目
     * @param parentId
     * @param cb
     */
    public void GetLatest(String parentId,JJCallBack cb){
        String lastestUrl = serverUrl + "/Users/" + UserId + "/Items/Latest?";
        lastestUrl += "Limit=16&Fields=PrimaryImageAspectRatio%2CBasicSyncInfo%2CPath";
        lastestUrl += "&ImageTypeLimit=1&EnableImageTypes=Primary,Backdrop,Thumb";
        lastestUrl += "&ParentId=" + parentId;

        SendGet(lastestUrl,new JJCallBack(){
            @Override
            public void onSuccess(String str) {
                JsonArray latestObj = strToGson(str,JsonArray.class);
                if(null != latestObj) {
                    cb.onSuccess(latestObj);
                }
            }
        });
    }

    /**
     * 获取最近播放
     * @param cb
     */
    public void GetResume(JJCallBack cb){
        String resumeUrl = serverUrl + "/Users/" + UserId + "/Items/Resume?";
        resumeUrl += "Limit=12&Recursive=true&Fields=PrimaryImageAspectRatio,BasicSyncInfo";
        resumeUrl += "&ImageTypeLimit=1&EnableImageTypes=Primary,Backdrop,Thumb";
        resumeUrl += "&EnableTotalRecordCount=false&MediaTypes=Video";

        SendGet(resumeUrl,new JJCallBack(){
            @Override
            public void onSuccess(String str) {
                JsonObject resumeObj = strToGson(str,JsonObject.class);
                if(null != resumeObj) {
                    JsonArray resumes = jeFromGson(resumeObj, "Items").getAsJsonArray();
                    cb.onSuccess(resumes);
                }
            }
        });
    }

    /**
     * 获取首页
     * @param cb
     */
    public void GetViews(JJCallBack cb){
        String viewsUrl = serverUrl + "/Users/" + UserId + "/Views";
        SendGet(viewsUrl,new JJCallBack(){
            @Override
            public void onSuccess(String str) {
                JsonObject viewsObj = strToGson(str,JsonObject.class);
                if(null!=viewsObj){
                    JsonArray views = jeFromGson(viewsObj,"Items").getAsJsonArray();
                    cb.onSuccess(views);
                }
            }
        });
    }

    /**
     * 获取项目详情
     * @param itemid
     * @param cb
     */
    public void GetItemInfo(String itemid,JJCallBack cb){
        String url = serverUrl + "/Users/" + UserId + "/Items/" + itemid;
        SendGet(url,new JJCallBack(){
            @Override
            public void onSuccess(String str) {
                JsonObject item = strToGson(str,JsonObject.class);
                if(null != item){
                    cb.onSuccess(item);
                }
            }
        });
    }

    /**
     * 验证用户名密码
     * @param username
     * @param password
     * @param cb
     */
    public void authenticateByName(String username,String password,JJCallBack cb){
        String url = serverUrl + "/Users/authenticatebyname";
        String reqjson = "{\"Username\":\"" + username + "\",\"Pw\":\"" + password + "\"}";
        SendPost(url,reqjson,new JJCallBack(){
            @Override
            public void onSuccess(String str) {
                JsonObject userObj = strToGson(str, JsonObject.class);
                if (userObj != null) {
                    String userId = jeFromGson(jeFromGson(userObj, "User").getAsJsonObject(),"Id").getAsString();
                    String Token = jeFromGson(userObj,"AccessToken").getAsString();
                    if (!Token.equals("")) {
                        UserId = userId;
                        AccessToken = Token;
                        cb.onSuccess(true);
                    }else {
                        cb.onSuccess(false);
                    }
                }else {
                    cb.onSuccess(false);
                }
            }
        });
    }

    /**
     * 验证服务器地址
     * @param url
     * @param cb
     */
    public void VerityServerUrl(String url,JJCallBack cb){
        SendGet(url + "/system/info/public",new JJCallBack(){
            @Override
            public void onSuccess(String str) {
                JsonObject serverInfo = strToGson(str,JsonObject.class);
                if (serverInfo != null) {
                    String ServerId = jeFromGson(serverInfo,"Id").getAsString();
                    if (ServerId == null || ServerId.length() == 0) {
                        cb.onSuccess(false);
                    } else {
                        cb.onSuccess(true);
                    }
                }
            }
        });
    }

    /**
     * Get
     * @param url
     * @param cb
     */
    public void SendGet(String url, JJCallBack cb){
        OkGo.<String>get(url).headers(headers).execute(new AbsCallback<String>() {
            @Override
            public String convertResponse(okhttp3.Response response) throws Throwable {
                String result = "";
                if(null != response.body()){
                    result = response.body().string();
                }
                return result;
            }

            @Override
            public void onSuccess(Response<String> response) {
                cb.onSuccess(response.body());
            }
        });
    }

    /**
     * Post
     * @param url
     * @param jsonStr
     * @param cb
     */
    public void SendPost(String url, String jsonStr, JJCallBack cb){
        OkGo.<String>post(url).headers(headers).upJson(jsonStr).execute(new AbsCallback<String>() {
            @Override
            public String convertResponse(okhttp3.Response response) throws Throwable {
                String result = "";
                if(null != response.body()){
                    result = response.body().string();
                }
                return result;
            }

            @Override
            public void onSuccess(Response<String> response) {
                cb.onSuccess(response.body());
            }
        });
    }

    /**
     * Json字符串转Gson对象
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

    /**
     * 按key获取JsonElement
     * @param obj
     * @param key
     * @return
     */
    public static JsonElement jeFromGson(JsonObject obj,String key){
        JsonElement je = new Gson().fromJson("",JsonElement.class);
        if(obj.has(key)){
            je = obj.get(key);
        }
        return je;
    }

    public class JJCallBack implements JCallBack{

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
    }

    public interface JCallBack{
        void onSuccess(String str);
        void onSuccess(Boolean bool);
        void onSuccess(JsonObject jsonObject);
        void onSuccess(JsonArray jsonArray);
    }
}
