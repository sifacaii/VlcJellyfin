package org.sifacai.vlcjellyfin;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.Timer;
import java.util.TimerTask;

public class VlcPlayerActivity extends BaseActivity implements MediaPlayer.EventListener
                                                                    , View.OnClickListener
                                                                    , PopupMenu.OnMenuItemClickListener{
    private static final String TAG = "VLC播放器";
    public final int TrackType_Subtitle = 0;
    public final int TrackType_Audio = 1;

    private Activity mActivity;
    private MediaPlayer mediaPlayer;
    private LibVLC libVLC;
    private VLCVideoLayout vlcVideoLayout;

    private RelativeLayout Controller; //总控
    private LinearLayout ControllerTop;
    private LinearLayout ControllerBottom;
    private LinearLayout ControllerBottomTop;
    private LinearLayout ControllerBottomBottom;
    private TextView videoTitle;
    private TextView currTime;
    private TextView countTime;
    private ImageView preBtn;
    private ImageView nextBtn;
    private ImageView playPauseBtn;
    private ImageView stopBtn;
    private ImageView subTracksBtn;
    private ImageView audioTracksBtn;
    private ImageView pauseFlag;
    private SeekBar currPostion;

    private Timer progressTime = null;

    private PopupMenu subTrackMenu = null; //字幕菜单
    private PopupMenu audioTrackMenu = null; //单轨菜单

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vlc_player);

        mActivity = this;
        initVlc();
        initController();
        play();
    }

    /**
     * 初始化播放器
     */
    private void initVlc() {
        vlcVideoLayout = findViewById(R.id.VideoView);
        libVLC = new LibVLC(this);
        mediaPlayer = new MediaPlayer(libVLC);
        mediaPlayer.attachViews(vlcVideoLayout, null, true, false);
        mediaPlayer.setEventListener(this);
    }

    @Override
    public void onEvent(MediaPlayer.Event event) {
        switch (event.type) {
            case MediaPlayer.Event.Playing: //媒体打开成功
                Hide();
                pauseFlag.setVisibility(View.GONE);
                Log.d(TAG, "onEvent: 打开成功");
                initMenu();
                break;
            case MediaPlayer.Event.Paused: //暂停
                pauseFlag.setVisibility(View.VISIBLE);
                break;
            case MediaPlayer.Event.Stopped:
                playNext();
                break;
            case MediaPlayer.Event.Opening:  //媒体打开
                Log.d(TAG, "onEvent: 媒体打开");
                break;
            case MediaPlayer.Event.Buffering: //媒体加载public float getBuffering() 获取加载视频流的进度0-100
                int Buffering = (int) event.getBuffering();
                if (null == alertDialogLoading || !alertDialogLoading.isShowing()) {
                    showLoadingDialog();
                }
                setLoadingText("加载进度：%" + Buffering);
                if(Buffering >= 100){
                    Log.d(TAG, "onEvent: 取消loading");
                    dismissLoadingDialog();
                }
                Log.d(TAG, "onEvent: 加载：" + Buffering);
                break;
            case MediaPlayer.Event.EndReached://媒体播放结束
                Log.d(TAG, "onEvent: EndReached");
                break;
            case MediaPlayer.Event.EncounteredError://媒体播放错误
                Log.d(TAG, "onEvent: EncounteredError");
                break;
            case MediaPlayer.Event.TimeChanged://视频时间变化
                //setSeekBar(event.getTimeChanged());
                break;
            case MediaPlayer.Event.PositionChanged://视频总时长的百分比
                Log.d(TAG, "onEvent: 百分之:" + mediaPlayer.getPosition());
                break;
            case MediaPlayer.Event.SeekableChanged:
                break;
            case MediaPlayer.Event.PausableChanged:
                Log.d(TAG, "onEvent: PausableChanged");
                break;
            case MediaPlayer.Event.LengthChanged:
                Log.d(TAG, "onEvent: LengthChanged");
                break;
            case MediaPlayer.Event.Vout://当图像输出
                Log.d(TAG, "onEvent: Vout");
                break;
            case MediaPlayer.Event.ESAdded:
                Log.d(TAG, "onEvent: ESAdded");
                break;
            case MediaPlayer.Event.ESDeleted:
                Log.d(TAG, "onEvent: ESDeleted");
                break;
            case MediaPlayer.Event.ESSelected:
                Log.d(TAG, "onEvent: ESSelected");
                break;
            case MediaPlayer.Event.RecordChanged:
                Log.d(TAG, "onEvent: RecordChanged");
                break;
        }
    }

    /**
     * 初始化控制器
     */
    private void initController() {
        Controller = findViewById(R.id.Controller); // 总布局
        ControllerTop = findViewById(R.id.ControllerTop);
        ControllerBottom = findViewById(R.id.ControllerBottom);
        ControllerBottomTop = findViewById(R.id.ControllerBottomTop);
        ControllerBottomBottom = findViewById(R.id.ControllerBottomBottom);
        videoTitle = findViewById(R.id.videoTitle); // 标题
        currTime = findViewById(R.id.currTime);
        countTime = findViewById(R.id.countTime);
        preBtn = findViewById(R.id.preBtn);
        nextBtn = findViewById(R.id.nextBtn);
        playPauseBtn = findViewById(R.id.playPauseBtn);
        stopBtn = findViewById(R.id.stopBtn);
        pauseFlag = findViewById(R.id.pauseFlag);
        currPostion = findViewById(R.id.currPostion);
        preBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        playPauseBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
        pauseFlag.setOnClickListener(this);
        //currPostion.setOnSeekBarChangeListener(this);
        currPostion.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                boolean rv = false;
                int keycode = keyEvent.getKeyCode();
                if(keycode == KeyEvent.KEYCODE_DPAD_RIGHT){
                    rv = setTimeOnSeekBar(mediaPlayer.getTime() + (long) (mediaPlayer.getLength() * 0.05));
                }else if(keycode == KeyEvent.KEYCODE_DPAD_LEFT){
                    rv = setTimeOnSeekBar(mediaPlayer.getTime() - (long) (mediaPlayer.getLength() * 0.05));
                }
                return rv;
            }
        });
    }

    /**
     * 初始化字幕和单轨菜单
     */
    private void initMenu(){
        MediaPlayer.TrackDescription[] subTrackList = mediaPlayer.getSpuTracks();
        MediaPlayer.TrackDescription[] audioTrackList = mediaPlayer.getAudioTracks();

        subTracksBtn = findViewById(R.id.subTracks);
        audioTracksBtn = findViewById(R.id.audioTracks);
        if(null != subTrackList && subTrackList.length > 1){
            initSubTrackMenu(subTrackList);
        }else{
            subTracksBtn.setVisibility(View.GONE);
        }

        if(null != audioTrackList && audioTrackList.length > 1) {
            initAudioTrackMenu(audioTrackList);
        }else{
            audioTracksBtn.setVisibility(View.GONE);
        }

    }

    /**
     * 初始化字幕菜单
     * @param subTrackList
     */
    private void initSubTrackMenu(MediaPlayer.TrackDescription[] subTrackList){
        subTrackMenu = new PopupMenu(this,subTracksBtn);
        for(int i=0;i<subTrackList.length;i++){
            subTrackMenu.getMenu().add(0,i,i,subTrackList[i].name);
        }
        subTrackMenu.setOnMenuItemClickListener(this);
        subTracksBtn.setOnClickListener(this);
    }

    private void initAudioTrackMenu(MediaPlayer.TrackDescription[] audioTrackList){
        audioTrackMenu = new PopupMenu(this,audioTracksBtn);
        for(int i=0;i<audioTrackList.length;i++){
            audioTrackMenu.getMenu().add(TrackType_Audio,i,i,audioTrackList[i].name);
        }
        audioTrackMenu.setOnMenuItemClickListener(this);
        audioTracksBtn.setOnClickListener(this);
    }


    /**
     * 显示控制器
     */
    public void Show() {
        Show(6);
    }

    /**
     * 显示控制器
     */
    public void Show(int sec) {
        if (ControllerTop.getVisibility() == View.GONE) {
            ControllerTop.setVisibility(View.VISIBLE);
        }
        if (ControllerBottom.getVisibility() == View.GONE) {
            ControllerBottom.setVisibility(View.VISIBLE);
            progressTime = new Timer();
            progressTime.schedule(new TimerTask() {
                @Override
                public void run() {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setSeekBar(mediaPlayer.getTime());
                        }
                    });
                }
            }, 500, 500);
        }
    }

    /**
     * 设置进度条时间
     */
    public void setSeekBar(Long p) {
        if (ControllerBottom.getVisibility() == View.VISIBLE) {
            double i = (double) p / 1000;
            long duration = mediaPlayer.getLength();
            if (duration > 0) {
                long pos = 1000L * p / duration;
                currPostion.setProgress((int) pos);
            }
            currTime.setText(TrickToTime(p));
            countTime.setText(TrickToTime(duration));
        }
    }

    /**
     * 隐藏控制器
     */
    public void Hide() {
        if (ControllerTop.getVisibility() == View.VISIBLE) {
            ControllerTop.setVisibility(View.GONE);
        }
        if (ControllerBottom.getVisibility() == View.VISIBLE) {
            ControllerBottom.setVisibility(View.GONE);
            if (progressTime != null) {
                progressTime.cancel();
                progressTime = null;
            }
        }
    }

    /**
     * 开始播放
     */
    public void play() {
        if (Utils.playList.size() > 0) {
            if (Utils.playIndex < Utils.playList.size()) {
                Video v = Utils.playList.get(Utils.playIndex);
                videoTitle.setText(v.Name);
                mediaPlayer.play(Uri.parse(v.Url));
            }
        } else {
            stop();
        }
    }

    /**
     * 播放下一集
     */
    public void playNext() {
        Utils.playIndex += 1;
        play();
    }

    /**
     * 上一集
     */
    public void playPre() {
        if (Utils.playIndex > 0) {
            Utils.playIndex -= 1;
            play();
        }
    }


    /**
     * 停止播放并结束Activity
     */
    public void stop() {
        if (progressTime != null) {
            progressTime.cancel();
            progressTime = null;
        }
        mediaPlayer.stop();
        mediaPlayer.release();
        libVLC.release();
        finish();
    }

    /**
     * 播放或暂停
     */
    public void playOrpause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            pauseFlag.setVisibility(View.VISIBLE);
        } else {
            mediaPlayer.play();
        }
    }

    /**
     * 设置播放器位置
     */
    public boolean setTimeOnSeekBar(Long p){
        if (p < mediaPlayer.getLength() && p > 0) {
            mediaPlayer.setTime(p);
            setSeekBar(p);
        }
        return true;
    }

    /**
     * 进度转时间
     *
     * @param trick
     * @return
     */
    public static String TrickToTime(long trick) {
        String time = "";
        long totalSeconds = trick / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;

        time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return time;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (ControllerBottom.getVisibility() == View.GONE) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    Show();
                    return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    mediaPlayer.setTime(mediaPlayer.getTime() + 30000);
                    return true;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    mediaPlayer.setTime(mediaPlayer.getTime() - 10000);
                    return true;
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    playOrpause();
                    return true;
                case KeyEvent.KEYCODE_ESCAPE:
                case KeyEvent.KEYCODE_BACK:
                    stop();
                    return true;
                //退出
            }
        } else {
            switch (keyCode) {
                case KeyEvent.KEYCODE_ESCAPE:
                case KeyEvent.KEYCODE_BACK:
                    Hide();
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.nextBtn) {
            playNext();
        } else if (id == R.id.preBtn) {
            playPre();
        } else if (id == R.id.playPauseBtn) {
            playOrpause();
        } else if (id == R.id.stopBtn) {
            stop();
        } else if (id == R.id.subTracks){
            subTrackMenu.show();
            Log.d(TAG, "onClick: 当前轨ID：" + mediaPlayer.getSpuTrack());
        } else if (id == R.id.audioTracks){
            audioTrackMenu.show();
            Log.d(TAG, "onClick: 当前单轨ID：" + mediaPlayer.getAudioTrack());
        }
    }


    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        int groupid = menuItem.getGroupId();
        int itemid = menuItem.getItemId();
        if(groupid == TrackType_Subtitle){
            mediaPlayer.setSpuTrack(itemid);
            return true;
        }else if(groupid == TrackType_Audio){
            mediaPlayer.setAudioTrack(itemid);
            return true;
        }
        return false;
    }
}