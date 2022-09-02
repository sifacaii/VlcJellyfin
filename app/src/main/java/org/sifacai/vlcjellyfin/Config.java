package org.sifacai.vlcjellyfin;

import android.content.Context;
import android.content.SharedPreferences;

public class Config {
    private Context context;
    private String JellyfinUrl = "";
    private String UserName = "";
    private String PassWord = "";
    private String SortBy = "DateCreated";
    private String SortOrder = "Descending";
    private boolean PlayStartInBegin; //是否从头开始播放 （接上次播放进度）
    private boolean HAACC;  //硬解
    private boolean FORCE_HAACC;  //强制硬解

    public boolean isPlayStartInBegin() {
        return PlayStartInBegin;
    }

    public void setPlayStartInBegin(boolean playStartInBegin) {
        PlayStartInBegin = playStartInBegin;
        saveConfigToSP("PlayStartInBegin",playStartInBegin);
    }

    public Config(Context context) {
        this.context = context;
        getConfigFromSP();
    }

    public String getJellyfinUrl() {
        return JellyfinUrl;
    }

    public void setJellyfinUrl(String jellyfinUrl) {
        JellyfinUrl = jellyfinUrl;
        saveConfigToSP("url",jellyfinUrl);
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
        saveConfigToSP("username",userName);
    }

    public String getPassWord() {
        return PassWord;
    }

    public void setPassWord(String passWord) {
        PassWord = passWord;
        saveConfigToSP("password",passWord);
    }

    public String getSortBy() {
        return SortBy;
    }

    public void setSortBy(String sortBy) {
        SortBy = sortBy;
        saveConfigToSP("sortby",sortBy);
    }

    public String getSortOrder() {
        return SortOrder;
    }

    public void setSortOrder(String sortOrder) {
        SortOrder = sortOrder;
        saveConfigToSP("sortorder",sortOrder);
    }

    public boolean isHAACC() {
        return HAACC;
    }

    public void setHAACC(boolean HAACC) {
        this.HAACC = HAACC;
        saveConfigToSP("HAACC",HAACC);
    }

    public boolean isFORCE_HAACC() {
        return FORCE_HAACC;
    }

    public void setFORCE_HAACC(boolean FORCE_HAACC) {
        this.FORCE_HAACC = FORCE_HAACC;
        saveConfigToSP("FORCE_HAACC",FORCE_HAACC);
    }

    /**
     * 读取配置
     */
    public void getConfigFromSP() {
        SharedPreferences sp = context.getSharedPreferences("Jellyfin", context.MODE_PRIVATE);
        this.JellyfinUrl = sp.getString("url", "http://");
        this.UserName = sp.getString("username", "");
        this.PassWord = sp.getString("password", "");
        this.SortBy = sp.getString("sortby","DateCreated");
        this.SortOrder = sp.getString("sortorder","Descending");
        this.HAACC = sp.getBoolean("HAACC",true);
        this.FORCE_HAACC = sp.getBoolean("FORCE_HAACC",false);
        this.PlayStartInBegin = sp.getBoolean("PlayStartInBegin",true);
    }

    /**
     * 保存单项配置
     */
    public void saveConfigToSP(String key, String value) {
        SharedPreferences sp = context.getSharedPreferences("Jellyfin", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * 保存单项配置
     */
    public void saveConfigToSP(String key, boolean value) {
        SharedPreferences sp = context.getSharedPreferences("Jellyfin", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * 清除
     */
    public void clear() {
        SharedPreferences sp = context.getSharedPreferences("Jellyfin", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
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

        public static String findName(String value){
            String name = "";
            for (SortByType sbt:SortByType.values()) {
                if(sbt.value.equals(value)){
                    name = sbt.name();
                }
            }
            return name;
        }
    }

    public enum SotrOrderType{
        升序("Ascending"),
        降序("Descending");

        public String value;

        SotrOrderType(String value) {
            this.value = value;
        }

        public static String findName(String value){
            String name = "";
            for (SotrOrderType sot:SotrOrderType.values()) {
                if(sot.value.equals(value)){
                    name = sot.name();
                }
            }
            return name;
        }
    }
}
