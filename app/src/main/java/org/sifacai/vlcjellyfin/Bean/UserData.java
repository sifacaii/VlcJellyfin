/**
  * Copyright 2022 json.cn 
  */
package org.sifacai.vlcjellyfin.Bean;
import java.util.Date;

/**
 * Auto-generated: 2022-09-01 14:6:30
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
public class UserData {

    private long PlaybackPositionTicks;
    private int PlayCount;
    private boolean IsFavorite;
    private Date LastPlayedDate;
    private boolean Played;
    private String Key;
    private double PlayedPercentage;

    public boolean isFavorite() {
        return IsFavorite;
    }

    public void setFavorite(boolean favorite) {
        IsFavorite = favorite;
    }

    public boolean isPlayed() {
        return Played;
    }

    public double getPlayedPercentage() {
        return PlayedPercentage;
    }

    public void setPlayedPercentage(double playedPercentage) {
        PlayedPercentage = playedPercentage;
    }

    public void setPlaybackPositionTicks(long PlaybackPositionTicks) {
         this.PlaybackPositionTicks = PlaybackPositionTicks;
     }
     public long getPlaybackPositionTicks() {
         return PlaybackPositionTicks;
     }

    public void setPlayCount(int PlayCount) {
         this.PlayCount = PlayCount;
     }
     public int getPlayCount() {
         return PlayCount;
     }

    public void setIsFavorite(boolean IsFavorite) {
         this.IsFavorite = IsFavorite;
     }
     public boolean getIsFavorite() {
         return IsFavorite;
     }

    public void setLastPlayedDate(Date LastPlayedDate) {
         this.LastPlayedDate = LastPlayedDate;
     }
     public Date getLastPlayedDate() {
         return LastPlayedDate;
     }

    public void setPlayed(boolean Played) {
         this.Played = Played;
     }
     public boolean getPlayed() {
         return Played;
     }

    public void setKey(String Key) {
         this.Key = Key;
     }
     public String getKey() {
         return Key;
     }

}