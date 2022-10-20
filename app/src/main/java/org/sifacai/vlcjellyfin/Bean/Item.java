/**
  * Copyright 2022 json.cn 
  */
package org.sifacai.vlcjellyfin.Bean;
import java.util.Date;
import java.util.List;

/**
 * Auto-generated: 2022-09-01 14:6:30
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
public class Item {

    private String Name;
    private String OriginalTitle;
    private String ServerId;
    private String Id;
    private String Etag;
    private Date DateCreated;
    private boolean CanDelete;
    private boolean CanDownload;
    private boolean HasSubtitles;
    private String Container;
    private String SortName;
    private String PremiereDate;
    private List<ExternalUrls> ExternalUrls;
    private List<MediaSources> MediaSources;
    private List<String> ProductionLocations;
    private String Path;
    private boolean EnableMediaSourceDisplay;
    private String OfficialRating;
    private String ChannelId;
    private String Overview;
    private List<String> Taglines;
    private List<String> Genres;
    private String CommunityRating;
    private long RunTimeTicks;
    private String PlayAccess;
    private String ProductionYear;
//    private List<String> RemoteTrailers;
    private ProviderIds ProviderIds;
    private boolean IsHD;
    private boolean IsFolder;
    private String ParentId;
    private String Type;
    private List<People> People;
    private List<Studios> Studios;
    private List<GenreItems> GenreItems;
    private int LocalTrailerCount;
    private UserData UserData;
    private int SpecialFeatureCount;
    private String DisplayPreferencesId;
    private List<String> Tags;
    private double PrimaryImageAspectRatio;
    private List<MediaStreams> MediaStreams;
    private String VideoType;
    private ImageTags ImageTags;
    private List<String> BackdropImageTags;
    private List<Chapters> Chapters;
    private String LocationType;
    private String MediaType;
    private List<String> LockedFields;
    private boolean LockData;
    private int Width;
    private int Height;

    public int getPartCount() {
        return PartCount;
    }

    public void setPartCount(int partCount) {
        PartCount = partCount;
    }

    private int PartCount;
    private String SeriesId;
    private String SeriesName;
    private String SeasonId;
    //private List<String> ProductionLocations;
    //private

    public String getSeriesId() {
        return SeriesId;
    }

    public void setSeriesId(String seriesId) {
        SeriesId = seriesId;
    }

    public String getSeasonId() {
        return SeasonId;
    }

    public void setSeasonId(String seasonId) {
        SeasonId = seasonId;
    }

    private String SeasonName;

    public boolean isHasSubtitles() {
        return HasSubtitles;
    }

    public boolean isHD() {
        return IsHD;
    }

    public void setHD(boolean HD) {
        IsHD = HD;
    }

    public boolean isFolder() {
        return IsFolder;
    }

    public void setFolder(boolean folder) {
        IsFolder = folder;
    }

    public String getSeriesName() {
        return SeriesName;
    }

    public void setSeriesName(String seriesName) {
        SeriesName = seriesName;
    }

    public String getSeasonName() {
        return SeasonName;
    }

    public void setSeasonName(String seasonName) {
        SeasonName = seasonName;
    }

    public void setName(String Name) {
         this.Name = Name;
     }
     public String getName() {
         return Name;
     }

    public void setOriginalTitle(String OriginalTitle) {
         this.OriginalTitle = OriginalTitle;
     }
     public String getOriginalTitle() {
         return OriginalTitle;
     }

    public void setServerId(String ServerId) {
         this.ServerId = ServerId;
     }
     public String getServerId() {
         return ServerId;
     }

    public void setId(String Id) {
         this.Id = Id;
     }
     public String getId() {
         return Id;
     }

    public void setEtag(String Etag) {
         this.Etag = Etag;
     }
     public String getEtag() {
         return Etag;
     }

    public void setDateCreated(Date DateCreated) {
         this.DateCreated = DateCreated;
     }
     public Date getDateCreated() {
         return DateCreated;
     }

    public void setCanDelete(boolean CanDelete) {
         this.CanDelete = CanDelete;
     }
     public boolean getCanDelete() {
         return CanDelete;
     }

    public void setCanDownload(boolean CanDownload) {
         this.CanDownload = CanDownload;
     }
     public boolean getCanDownload() {
         return CanDownload;
     }

    public void setHasSubtitles(boolean HasSubtitles) {
         this.HasSubtitles = HasSubtitles;
     }
     public boolean getHasSubtitles() {
         return HasSubtitles;
     }

    public void setContainer(String Container) {
         this.Container = Container;
     }
     public String getContainer() {
         return Container;
     }

    public void setSortName(String SortName) {
         this.SortName = SortName;
     }
     public String getSortName() {
         return SortName;
     }

    public void setPremiereDate(String PremiereDate) {
         this.PremiereDate = PremiereDate;
     }
     public String getPremiereDate() {
         return PremiereDate;
     }

    public void setExternalUrls(List<ExternalUrls> ExternalUrls) {
         this.ExternalUrls = ExternalUrls;
     }
     public List<ExternalUrls> getExternalUrls() {
         return ExternalUrls;
     }

    public void setMediaSources(List<MediaSources> MediaSources) {
         this.MediaSources = MediaSources;
     }
     public List<MediaSources> getMediaSources() {
         return MediaSources;
     }

    public void setProductionLocations(List<String> ProductionLocations) {
         this.ProductionLocations = ProductionLocations;
     }
     public List<String> getProductionLocations() {
         return ProductionLocations;
     }

    public void setPath(String Path) {
         this.Path = Path;
     }
     public String getPath() {
         return Path;
     }

    public void setEnableMediaSourceDisplay(boolean EnableMediaSourceDisplay) {
         this.EnableMediaSourceDisplay = EnableMediaSourceDisplay;
     }
     public boolean getEnableMediaSourceDisplay() {
         return EnableMediaSourceDisplay;
     }

    public void setOfficialRating(String OfficialRating) {
         this.OfficialRating = OfficialRating;
     }
     public String getOfficialRating() {
         return OfficialRating;
     }

    public void setChannelId(String ChannelId) {
         this.ChannelId = ChannelId;
     }
     public String getChannelId() {
         return ChannelId;
     }

    public void setOverview(String Overview) {
         this.Overview = Overview;
     }
     public String getOverview() {
         return Overview;
     }

    public void setTaglines(List<String> Taglines) {
         this.Taglines = Taglines;
     }
     public List<String> getTaglines() {
         return Taglines;
     }

    public void setGenres(List<String> Genres) {
         this.Genres = Genres;
     }
     public List<String> getGenres() {
         return Genres;
     }

    public void setCommunityRating(String CommunityRating) {
         this.CommunityRating = CommunityRating;
     }
     public String getCommunityRating() {
         return CommunityRating;
     }

    public void setRunTimeTicks(long RunTimeTicks) {
         this.RunTimeTicks = RunTimeTicks;
     }
     public long getRunTimeTicks() {
         return RunTimeTicks;
     }

    public void setPlayAccess(String PlayAccess) {
         this.PlayAccess = PlayAccess;
     }
     public String getPlayAccess() {
         return PlayAccess;
     }

    public void setProductionYear(String ProductionYear) {
         this.ProductionYear = ProductionYear;
     }
     public String getProductionYear() {
         return ProductionYear;
     }

//    public void setRemoteTrailers(List<String> RemoteTrailers) {
//         this.RemoteTrailers = RemoteTrailers;
//     }
//     public List<String> getRemoteTrailers() {
//         return RemoteTrailers;
//     }

    public void setProviderIds(ProviderIds ProviderIds) {
         this.ProviderIds = ProviderIds;
     }
     public ProviderIds getProviderIds() {
         return ProviderIds;
     }

    public void setIsHD(boolean IsHD) {
         this.IsHD = IsHD;
     }
     public boolean getIsHD() {
         return IsHD;
     }

    public void setIsFolder(boolean IsFolder) {
         this.IsFolder = IsFolder;
     }
     public boolean getIsFolder() {
         return IsFolder;
     }

    public void setParentId(String ParentId) {
         this.ParentId = ParentId;
     }
     public String getParentId() {
         return ParentId;
     }

    public void setType(String Type) {
         this.Type = Type;
     }
     public String getType() {
         return Type;
     }

    public void setPeople(List<People> People) {
         this.People = People;
     }
     public List<People> getPeople() {
         return People;
     }

    public void setStudios(List<Studios> Studios) {
         this.Studios = Studios;
     }
     public List<Studios> getStudios() {
         return Studios;
     }

    public void setGenreItems(List<GenreItems> GenreItems) {
         this.GenreItems = GenreItems;
     }
     public List<GenreItems> getGenreItems() {
         return GenreItems;
     }

    public void setLocalTrailerCount(int LocalTrailerCount) {
         this.LocalTrailerCount = LocalTrailerCount;
     }
     public int getLocalTrailerCount() {
         return LocalTrailerCount;
     }

    public void setUserData(UserData UserData) {
         this.UserData = UserData;
     }
     public UserData getUserData() {
         return UserData;
     }

    public void setSpecialFeatureCount(int SpecialFeatureCount) {
         this.SpecialFeatureCount = SpecialFeatureCount;
     }
     public int getSpecialFeatureCount() {
         return SpecialFeatureCount;
     }

    public void setDisplayPreferencesId(String DisplayPreferencesId) {
         this.DisplayPreferencesId = DisplayPreferencesId;
     }
     public String getDisplayPreferencesId() {
         return DisplayPreferencesId;
     }

    public void setTags(List<String> Tags) {
         this.Tags = Tags;
     }
     public List<String> getTags() {
         return Tags;
     }

    public void setPrimaryImageAspectRatio(double PrimaryImageAspectRatio) {
         this.PrimaryImageAspectRatio = PrimaryImageAspectRatio;
     }
     public double getPrimaryImageAspectRatio() {
         return PrimaryImageAspectRatio;
     }

    public void setMediaStreams(List<MediaStreams> MediaStreams) {
         this.MediaStreams = MediaStreams;
     }
     public List<MediaStreams> getMediaStreams() {
         return MediaStreams;
     }

    public void setVideoType(String VideoType) {
         this.VideoType = VideoType;
     }
     public String getVideoType() {
         return VideoType;
     }

    public void setImageTags(ImageTags ImageTags) {
         this.ImageTags = ImageTags;
     }
     public ImageTags getImageTags() {
         return ImageTags;
     }

    public void setBackdropImageTags(List<String> BackdropImageTags) {
         this.BackdropImageTags = BackdropImageTags;
     }
     public List<String> getBackdropImageTags() {
         return BackdropImageTags;
     }

    public void setChapters(List<Chapters> Chapters) {
         this.Chapters = Chapters;
     }
     public List<Chapters> getChapters() {
         return Chapters;
     }

    public void setLocationType(String LocationType) {
         this.LocationType = LocationType;
     }
     public String getLocationType() {
         return LocationType;
     }

    public void setMediaType(String MediaType) {
         this.MediaType = MediaType;
     }
     public String getMediaType() {
         return MediaType;
     }

    public void setLockedFields(List<String> LockedFields) {
         this.LockedFields = LockedFields;
     }
     public List<String> getLockedFields() {
         return LockedFields;
     }

    public void setLockData(boolean LockData) {
         this.LockData = LockData;
     }
     public boolean getLockData() {
         return LockData;
     }

    public void setWidth(int Width) {
         this.Width = Width;
     }
     public int getWidth() {
         return Width;
     }

    public void setHeight(int Height) {
         this.Height = Height;
     }
     public int getHeight() {
         return Height;
     }

}