package com.boredream.bdvideoplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.boredream.bdvideoplayer.bean.IVideoInfo;
import com.boredream.bdvideoplayer.listener.OnVideoControlListener;
import com.boredream.bdvideoplayer.listener.PlayerCallback;
import com.boredream.bdvideoplayer.listener.SimplePlayerCallback;
import com.boredream.bdvideoplayer.utils.NetworkUtils;
import com.boredream.bdvideoplayer.view.VideoBehaviorView;
import com.boredream.bdvideoplayer.view.VideoControllerView;
import com.boredream.bdvideoplayer.view.VideoProgressOverlay;
import com.boredream.bdvideoplayer.view.VideoSystemOverlay;
import com.ex.fivemao.R;
import com.ex.fivemao.VideoDuoPlayerActivity;

/**
 * 视频播放器View
 */
public class BDVideoView extends VideoBehaviorView {

    private SurfaceView mSurfaceView;
    private View mLoading;
    private VideoControllerView mediaController;
    private VideoSystemOverlay videoSystemOverlay;
    private VideoProgressOverlay videoProgressOverlay;
    private BDVideoPlayer mMediaPlayer;
    private RelativeLayout video_RelativeLayout;
    private int initWidth;
    private int initHeight;

    public boolean isLock() {
        return mediaController.isLock();
    }

    public BDVideoView(Context context) {
        super(context);
        init();
    }

    public BDVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BDVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.video_view, this);
        
        video_RelativeLayout = (RelativeLayout)findViewById(R.id.video_RelativeLayout);
        mSurfaceView = (SurfaceView) findViewById(R.id.video_surface);
        mLoading = findViewById(R.id.video_loading);
        mediaController = (VideoControllerView) findViewById(R.id.video_controller);
        videoSystemOverlay = (VideoSystemOverlay) findViewById(R.id.video_system_overlay);
        videoProgressOverlay = (VideoProgressOverlay) findViewById(R.id.video_progress_overlay);

        initPlayer();

        
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initWidth = getWidth();
                initHeight = getHeight();
                
                if (mMediaPlayer != null) {
                    mMediaPlayer.setDisplay(holder);
                    mMediaPlayer.openVideo();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        registerNetChangedReceiver();
    }

    public void addDoubleTapListener(OnDoubleTapListener listener){
    	mGestureDetector.setOnDoubleTapListener(listener);
    }
    public void addMediaPlayerCallback(PlayerCallback userPlayerCallback){
    	mMediaPlayer.addCallback(userPlayerCallback);
    }
    
    private void initPlayer() {
        mMediaPlayer = new BDVideoPlayer();
        
        mMediaPlayer.addCallback(new SimplePlayerCallback() {

            @Override
            public void onStateChanged(int curState) {
                switch (curState) {
                    case BDVideoPlayer.STATE_IDLE:
                        am.abandonAudioFocus(null);
                        break;
                    case BDVideoPlayer.STATE_PREPARING:
                        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                        break;
                }
            }

            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaController.updatePausePlay();
            }

            @Override
            public void onError(MediaPlayer mp, int what, int extra) {
                mediaController.checkShowError(false);
            }

            @Override
            public void onLoadingChanged(boolean isShow) {
                if (isShow) showLoading();
                else hideLoading();
            }

            @Override
            public void onPrepared(MediaPlayer mp) {
                mMediaPlayer.start();
                mediaController.show();
                mediaController.hideErrorView();
            }
        });
        
        mediaController.setMediaPlayer(mMediaPlayer);
    }

    private void showLoading() {
        mLoading.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        mLoading.setVisibility(View.GONE);
    }

    private boolean isBackgroundPause;

    public void onStop() {
        if (mMediaPlayer.isPlaying()) {
            // 如果已经开始且在播放，则暂停同时记录状态
            isBackgroundPause = true;
            mMediaPlayer.pause();
        }
    }

    public void onStart() {
        if (isBackgroundPause) {
            // 如果切换到后台暂停，后又切回来，则继续播放
            isBackgroundPause = false;
            mMediaPlayer.start();
        }
    }

    public void onDestroy() {
        mMediaPlayer.stop();
        mediaController.release();
        unRegisterNetChangedReceiver();
    }

    /**
     * 开始播放
     */
    public void startPlayVideo(final IVideoInfo video) {
        if (video == null) {
            return;
        }

        mMediaPlayer.reset();

        String videoPath = video.getVideoPath();
        mediaController.setVideoInfo(video);
        mMediaPlayer.setVideoPath(videoPath);
    }
    
    public void setVideoTitle(String title){
    	mediaController.setVideoTitle(title);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        mediaController.toggleDisplay();
        return super.onSingleTapUp(e);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (isLock()) {
            return false;
        }
        return super.onDown(e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (isLock()) {
            return false;
        }
        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    protected void endGesture(int behaviorType) {
        switch (behaviorType) {
            case VideoBehaviorView.FINGER_BEHAVIOR_BRIGHTNESS:
            case VideoBehaviorView.FINGER_BEHAVIOR_VOLUME:
                Log.i("DDD", "endGesture: left right");
                videoSystemOverlay.hide();
                break;
            case VideoBehaviorView.FINGER_BEHAVIOR_PROGRESS:
                Log.i("DDD", "endGesture: bottom");
                if(getSplitVideoTotalTime()>0){
            		
            	}else{
	                mMediaPlayer.seekTo(videoProgressOverlay.getTargetProgress());
	                videoProgressOverlay.hide();
            	}
                break;
        }
    }

    @Override
    protected void updateSeekUI(int delProgress) {
    	if(getSplitVideoTotalTime()>0){
    		
    	}else{
    		videoProgressOverlay.show(delProgress, mMediaPlayer.getCurrentPosition(),mMediaPlayer.getBufferPercentage(), mMediaPlayer.getDuration());
    	}
    }

    @Override
    protected void updateVolumeUI(int max, int progress) {
        videoSystemOverlay.show(VideoSystemOverlay.SystemType.VOLUME, max, progress);
    }

    @Override
    protected void updateLightUI(int max, int progress) {
        videoSystemOverlay.show(VideoSystemOverlay.SystemType.BRIGHTNESS, max, progress);
    }

    public void setOnVideoControlListener(OnVideoControlListener onVideoControlListener) {
        mediaController.setOnVideoControlListener(onVideoControlListener);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            getLayoutParams().width = initWidth;
//            getLayoutParams().height = initHeight;
        	getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
            getLayoutParams().height = 440;//1dp=2px generally
        } else {
            getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
            getLayoutParams().height = FrameLayout.LayoutParams.MATCH_PARENT;
        }

    }

    private NetChangedReceiver netChangedReceiver;

    public void registerNetChangedReceiver() {
        if (netChangedReceiver == null) {
            netChangedReceiver = new NetChangedReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            activity.registerReceiver(netChangedReceiver, filter);
        }
    }

    public void unRegisterNetChangedReceiver() {
        if (netChangedReceiver != null) {
            activity.unregisterReceiver(netChangedReceiver);
        }
    }

    private class NetChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Parcelable extra = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (extra != null && extra instanceof NetworkInfo) {
                NetworkInfo netInfo = (NetworkInfo) extra;

                if (NetworkUtils.isNetworkConnected(context) && netInfo.getState() != NetworkInfo.State.CONNECTED) {
                    // 网络连接的情况下只处理连接完成状态
                    return;
                }

                mediaController.checkShowError(true);
            }
        }
    }

	public int getCurrentPosition() {
		if(mMediaPlayer!=null){
			return mMediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	public int getDuration() {
		if(mMediaPlayer!=null){
			return mMediaPlayer.getDuration();
		}
		return 0;
	}

	public void stopVideoPlayer() {
		try{
			if(mMediaPlayer!=null){
				mMediaPlayer.stop();
			}
		}catch(Exception e){
			
		}
	}

	public void restart() {
		try{
			if(mMediaPlayer!=null){
				mMediaPlayer.restart();
			}
		}catch(Exception e){
			
		}
	}

	public void pausePlayer() {
		try{
			if(mMediaPlayer!=null){
				mMediaPlayer.pause();
			}
		}catch(Exception e){
			
		}
	}

	public void reset() {
		try{
			if(mMediaPlayer!=null){
				mMediaPlayer.reset();
			}
		}catch(Exception e){
			
		}
	}

	public void releasePlayer() {
		try{
			if(mMediaPlayer!=null){
				mMediaPlayer.release();
			}
		}catch(Exception e){
			
		}
	}
	
	public void setSplitVideoTotalTime(int splitTotalTime){
		mediaController.setSplitTotalTime(splitTotalTime);
	}
	public int getSplitVideoTotalTime(){
		return mediaController.getSplitTotalTime();
	}
	public void setSplitBaseTime(int splitBaseTime){
		mediaController.setSplitBaseTime(splitBaseTime);
	}

	public void setDuoPlayerActivity(VideoDuoPlayerActivity duoPlayerActivity) {
		mediaController.setDuoPlayerActivity(duoPlayerActivity);
	}
}
