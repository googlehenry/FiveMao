package com.boredream.bdvideoplayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
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
import com.boredream.bdvideoplayer.bean.VideoDetailInfo;
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
import com.ex.fivemao.exception.CrashHandler;

/**
 * 视频播放器View
 */
public class BDVideoSplitView extends VideoBehaviorView implements SurfaceHolder.Callback {

    private SurfaceView mSurfaceView;
    private SurfaceHolder surfaceHolder;
    private View mLoading;
    private VideoControllerView mediaController;
    private VideoSystemOverlay videoSystemOverlay;
    private VideoProgressOverlay videoProgressOverlay;
    private BDVideoPlayer mMediaPlayer;
    private RelativeLayout video_RelativeLayout;
    private int initWidth;
    private int initHeight;
    
    private NetChangedReceiver netChangedReceiver;
	private VideoDuoPlayerActivity duoPlayerActivity;
	private int splitsPreviousSplitsTime;
	private int splitsTotalTime;
	private String title;
	
    private MediaPlayer firstPlayer,     //负责播放进入视频播放界面后的第一段视频
    nextMediaPlayer, //负责一段视频播放结束后，播放下一段视频
    cachePlayer,     //负责setNextMediaPlayer的player缓存对象
    currentPlayer;   //负责当前播放视频段落的player对象
    
    private Map<String,Boolean> playerReadyStatus = new HashMap<String,Boolean>();
//    private boolean playersAreBeingStopping = false;
    //存放所有视频端的url
    private List<String> VideoListQueue = new ArrayList<String>();
    private List<String> urlsOriginal = new ArrayList<String>();
    //所有player对象的缓存
    private HashMap<String, MediaPlayer> playersCache = new HashMap<String, MediaPlayer>();
    //当前播放到的视频段落数
    private int currentVideoIndex;
    OnVideoControlListener onVideoControlListener;
    PrepareVideoThread prepareVideoThread;
    
    
    public boolean isLock() {
        return mediaController.isLock();
    }

    public BDVideoSplitView(Context context) {
        super(context);
        init();
    }

    public BDVideoSplitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BDVideoSplitView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        surfaceHolder = mSurfaceView.getHolder();
        
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initWidth = getWidth();
                initHeight = getHeight();
                Canvas canvas = holder.lockCanvas();  
                
                if(canvas != null) { 
                	BitmapDrawable bd = (BitmapDrawable)BDVideoSplitView.this.getBackground();
            		canvas.drawBitmap(bd.getBitmap(), 0, 0, null);
            		holder.unlockCanvasAndPost(canvas);  
                }
                
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
        
        //added by henry
        surfaceHolder.addCallback(this); // 因为这个类实现了SurfaceHolder.Callback接口，所以回调参数直接this
        
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
        
        bindeventsToBDPlayer(mMediaPlayer);
        
        mediaController.setMediaPlayer(mMediaPlayer);
    }

	private void bindeventsToBDPlayer(final BDVideoPlayer bdVideoPlayer) {
		bdVideoPlayer.addCallback(new SimplePlayerCallback() {
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
	        	mp.start();
	            mediaController.show();
	            mediaController.hideErrorView();
	        }
	    } );
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
    	if(mMediaPlayer!=null){
    		mMediaPlayer.stop();
    	}
        mediaController.release();
        unRegisterNetChangedReceiver();
        
        safelyStopMediaPlayer();
    }

	private void destroyPlayers() {
		//added by henry below
		if(firstPlayer==cachePlayer){
			if (firstPlayer != null) {
	            firstPlayer.release();
	            firstPlayer = null;
	        }
		}else{
			if (firstPlayer != null) {
	            firstPlayer.release();
	            firstPlayer = null;
	        }
			if (cachePlayer != null) {
	        	cachePlayer.release();
	        	cachePlayer = null;
	        }
		}
		
		if(firstPlayer==currentPlayer){
			if (firstPlayer != null) {
	            firstPlayer.release();
	            firstPlayer = null;
	        }
		}else{
			if (firstPlayer != null) {
	            firstPlayer.release();
	            firstPlayer = null;
	        }
			if (currentPlayer != null) {
	            currentPlayer.release();
	            currentPlayer = null;
	        }
		}
		
		if(nextMediaPlayer==currentPlayer){
			if (nextMediaPlayer != null) {
	            nextMediaPlayer.release();
	            nextMediaPlayer = null;
	        }
		}else{
			if (nextMediaPlayer != null) {
	            nextMediaPlayer.release();
	            nextMediaPlayer = null;
	        }
			if (currentPlayer != null) {
	            currentPlayer.release();
	            currentPlayer = null;
	        }
		}
        
        
		firstPlayer = null;
		currentPlayer = null;
		nextMediaPlayer = null;
		cachePlayer = null;
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
                if(getTotalTime(true)>0){
                	videoProgressOverlay.setBreakMaxBuffededProgress(true);
                	int target = videoProgressOverlay.getTargetProgress();
                	videoProgressOverlay.setBreakMaxBuffededProgress(true);
                	
                	duoPlayerActivity.dragtoSplitTimePoint(target);
                	videoProgressOverlay.hide();
            	}else{
	                mMediaPlayer.seekTo(videoProgressOverlay.getTargetProgress());
	                videoProgressOverlay.hide();
            	}
                break;
        }
    }

    @Override
    protected void updateSeekUI(int delProgress) {
    	if(getTotalTime(true)>0){
    		videoProgressOverlay.setBreakMaxBuffededProgress(true);
    		videoProgressOverlay.show(delProgress, getTotalCurrentTime(),getTotalCurrentTime()*100/getTotalSplitTime()+1, getTotalSplitTime());
    		videoProgressOverlay.setBreakMaxBuffededProgress(true);
    	}else{
    		videoProgressOverlay.show(delProgress, mMediaPlayer.getCurrentPosition(),mMediaPlayer.getBufferPercentage(), mMediaPlayer.getDuration());
    	}
    }

    private int getTotalCurrentTime() {
    	return mediaController.getSplitBaseTime()+getCurrentPosition();
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
    	this.onVideoControlListener = onVideoControlListener;
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
		}else 
		if(currentPlayer!=null){
			try{
				return currentPlayer.getCurrentPosition();
			}catch(Exception e){
				CrashHandler.logErrorToFile(e);
				return 0;
			}
		}
		return 0;
	}
	
	public MediaPlayer getCurrentPlayer(){
		return currentPlayer;
	}

	public int getDuration() {
		if(mMediaPlayer!=null){
			return mMediaPlayer.getDuration();
		}
		if(currentPlayer!=null){
			try{
				return currentPlayer.getDuration();
			}catch(Exception e){
				CrashHandler.logErrorToFile(e);
				return 0;
			}
		}
		return 0;
	}

	public void stopVideoPlayer() {
		try{
			
			mediaController.setSplitMediaPlayer(null);
		    //destroyPlayers();
		    safelyStopMediaPlayer();
			playersCache.clear();
	    	VideoListQueue.clear();
	    	urlsOriginal.clear();
		}catch(Exception e){
			CrashHandler.logErrorToFile(e);
		}
	}

	public void restart() {
		try{
			if(mMediaPlayer!=null){
				mMediaPlayer.restart();
			}
			if(currentPlayer!=null){
				currentPlayer.seekTo(0);
				currentPlayer.start();
			}
		}catch(Exception e){
			CrashHandler.logErrorToFile(e);
		}
	}

	public void startPlayer() {
		try{
			if(mMediaPlayer!=null){
				mMediaPlayer.start();
			}else 
			if(currentPlayer!=null){
				mediaController.toggleDisplay();
				currentPlayer.start();
			}
		}catch(Exception e){
			CrashHandler.logErrorToFile(e);
		}
	}
	public void pausePlayer() {
		try{
			if(mMediaPlayer!=null){
				mMediaPlayer.pause();
			}else 
			if(currentPlayer!=null){
				currentPlayer.pause();
			}
		}catch(Exception e){
			CrashHandler.logErrorToFile(e);
		}
	}

	public void reset() {
		try{
			if(mMediaPlayer!=null){
				mMediaPlayer.reset();
			}
			if(currentPlayer!=null){
				currentPlayer.reset();
			}
		}catch(Exception e){
			CrashHandler.logErrorToFile(e);
		}
	}

	public void releasePlayer() {
		try{
			if(mMediaPlayer!=null){
				mMediaPlayer.release();
			}
			if(currentPlayer!=null){
				currentPlayer.release();
			}
		}catch(Exception e){
			CrashHandler.logErrorToFile(e);
		}
	}
	
	public void setDuoPlayerActivity(VideoDuoPlayerActivity duoPlayerActivity) {
		this.duoPlayerActivity = duoPlayerActivity;
		mediaController.setDuoPlayerActivity(duoPlayerActivity);
	}

	@Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        // TODO 自动生成的方法存根

    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        //surfaceView创建完毕后，首先获取该直播间所有视频分段的url
//        getVideoUrls();
        //然后初始化播放手段视频的player对象
//        initFirstPlayer();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        // TODO 自动生成的方法存根

    }

    /*
     * 初始化播放首段视频的player
     */
    private void initFirstPlayer() {
        firstPlayer = new MediaPlayer();
        
        firstPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        VideoDetailInfo videoInfo = new VideoDetailInfo();
        videoInfo.title = title;
        mediaController.setVideoInfo(videoInfo);
        
        firstPlayer.setOnInfoListener(new OnInfoListener() {
			@Override
			public boolean onInfo(MediaPlayer mp, int what, int extra) {
				
				if(what==MediaPlayer.MEDIA_INFO_BUFFERING_START){
					showLoading();
					duoPlayerActivity.setLastDragDone(false);
				}else 
				if(what==MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START){
					hideLoading();
					duoPlayerActivity.setLastDragDone(true);
				}
				return false;
			}
		});
        
        firstPlayer.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
//				mp.start();
//				mp.setDisplay(surfaceHolder);
//				firstPlayer.setDisplay(surfaceHolder);
				hideLoading();
				duoPlayerActivity.setLastDragDone(true);
				duoPlayerActivity.setVideoPlayingHintVisibility(View.GONE);
				playerReadyStatus.put("firstPlayer", true);
				duoPlayerActivity.addFilmHotnessOnce();
			}
		});
        
        firstPlayer.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				mp.reset();
				duoPlayerActivity.setVideoPlayingHintTest("播放时出现异常,请返回上级重新播放。");
				return false;
			}
		});
        
        firstPlayer
                .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        onVideoPlayCompleted(mp);
                    }
                });

        //设置cachePlayer为该player对象
        currentPlayer = firstPlayer;
        cachePlayer = firstPlayer;
        initNexttPlayer();
        
        //player对象初始化完成后，开启播放
        //startPlayFirstVideo();
    }

    private void startPlayFirstVideo() {
        try {
        	mMediaPlayer = null;
        	mediaController.setMediaPlayer(null);
        	mediaController.setSplitMediaPlayer(firstPlayer);
        	mediaController.setSplitBaseTime(getPreviousTime(true));
            mediaController.setSplitTotalTime(getTotalTime(true));
            VideoDuoPlayerActivity.currentVideoFileIdx = currentVideoIndex;
            firstPlayer.setDisplay(surfaceHolder);
            firstPlayer.setScreenOnWhilePlaying(true);
        	firstPlayer.setDataSource(VideoListQueue.get(currentVideoIndex));
        	firstPlayer.prepare();
        	firstPlayer.start();
        	duoPlayerActivity.savePlayPoint(currentVideoIndex);
        } catch (Exception e) {
        	duoPlayerActivity.notifyError(400,e);
            CrashHandler.logErrorToFile(e);
        }
    }
    public int getTotalSplitTime(){
    	return mediaController.getSplitTotalTime();
    }
    /*
     * 新开线程负责初始化负责播放剩余视频分段的player对象,避免UI线程做过多耗时操作
     */
    
    private void initNexttPlayer() {
    	prepareVideoThread = new PrepareVideoThread();
    	prepareVideoThread.start();
    }
    /*
     * 负责处理一段视频播放过后，切换player播放下一段视频
     */
    private void onVideoPlayCompleted(MediaPlayer mp) {
    	try{
	        //get next player
	        currentVideoIndex = currentVideoIndex+1;
	        if(currentVideoIndex<VideoListQueue.size()){
		        mp.setDisplay(null);
		        currentPlayer = playersCache.get(String.valueOf(currentVideoIndex));
		        if (currentPlayer != null) {
		            currentPlayer.setDisplay(surfaceHolder);
		            currentPlayer.setScreenOnWhilePlaying(true);
		            mMediaPlayer = null;
		            mediaController.setMediaPlayer(null);
		            mediaController.setSplitMediaPlayer(currentPlayer);
		            mediaController.setSplitBaseTime(getPreviousTime(true));
		            mediaController.setSplitTotalTime(getTotalTime(true));
		            
		            VideoDuoPlayerActivity.currentVideoFileIdx = currentVideoIndex;
		            playerReadyStatus.put("firstPlayer", playerReadyStatus.get("nextMediaPlayer"));
		            playerReadyStatus.put("nextMediaPlayer", false);
		            duoPlayerActivity.savePlayPoint(currentVideoIndex);
		            initNexttPlayer() ;
		        }
	        }else{
	        	VideoDuoPlayerActivity.currentVideoFileIdx = currentVideoIndex - 1;
	        	duoPlayerActivity.notifyError(200, new Exception("电影播放完毕"));
	        }
    	}catch(Exception e){
    		duoPlayerActivity.notifyError(400,e);
    		CrashHandler.logErrorToFile(e);
    	}
    }
    
	public void startVideoSplits(String title, int startIdx,List<String> urls,List<String> urlsOriginalX){
		
//		int count = 10;
//		while(playersAreBeingStopping){
//			try {
//				Thread.sleep(1000);
//				count = count - 1;
//				if(count<0){
//					break;
//				}
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		if(playersAreBeingStopping){
//			duoPlayerActivity.setVideoPlayingHintTest("后台播放器正在准备中,本次播放失败,返回上级页面重试。");
//			duoPlayerActivity.setVideoPlayingHintVisibility(View.VISIBLE);
//		}else{
			VideoDetailInfo video = new VideoDetailInfo();
			video.title = title;
			mediaController.setVideoInfo(video);
			mediaController.setSplitMediaPlayer(null);
			
			playersCache.clear();
	    	VideoListQueue.clear();
	    	urlsOriginal.clear();
	    	
	    	
	    	this.VideoListQueue.addAll(urls);
	    	this.urlsOriginal.addAll(urlsOriginalX);
	    	currentVideoIndex = startIdx;
	    	this.title = title;
	
	    	safelyStopMediaPlayer();
	    	
	    	initFirstPlayer();
	    	startPlayFirstVideo();
	    	
//		}
    }

	public int getPreviousTime(boolean forceRefresh) {
		if(!forceRefresh){
			if(splitsPreviousSplitsTime >0){
				return splitsPreviousSplitsTime ;
			}
		}
			
		int countMinisecs = 0;
		if(urlsOriginal.size()>0){
			for(int i = 0; i <currentVideoIndex; i++){
				String url = urlsOriginal.get(i);
				String simpleName = url.substring(url.lastIndexOf("/")+1);
				String[] nameParts = simpleName.split("\\.")[0].split("_");
				int minisecs = Integer.parseInt(nameParts[nameParts.length-1]);
				countMinisecs = countMinisecs+minisecs;
			}
		}
		splitsPreviousSplitsTime = countMinisecs;
		return countMinisecs;
	}
	
	public int getTotalTime(boolean forceRefresh) {
		if(!forceRefresh){
			if(splitsTotalTime>0){
				return splitsTotalTime ;
			}
		}
		int countMinisecs = 0;
		if(urlsOriginal.size()>0){
			for(int i = 0; i <urlsOriginal.size(); i++){
				String url = urlsOriginal.get(i);
				String simpleName = url.substring(url.lastIndexOf("/")+1);
				String[] nameParts = simpleName.split("\\.")[0].split("_");
				int minisecs = Integer.parseInt(nameParts[nameParts.length-1]);
				countMinisecs = countMinisecs+minisecs;
			}
		}
		splitsTotalTime = countMinisecs;
		return countMinisecs;
	}

	
	
	private void safelyStopMediaPlayer() {
//		playersAreBeingStopping = true;
//		int count = 200;
//		while(firstPlayer!=null && playerReadyStatus.get("firstPlayer") !=null && !playerReadyStatus.get("firstPlayer")){
//			try {
//				Thread.sleep(1000);
//				count = count - 1;
//				if(count<0){
//					break;
//				}
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		if(playerReadyStatus.get("firstPlayer")!=null && playerReadyStatus.get("firstPlayer")){
//			if(firstPlayer!=null){
//				firstPlayer.release();
//				firstPlayer = null;
//			}
//		}
//		
//		int count2 = 200;
//		while(nextMediaPlayer!=null && playerReadyStatus.get("nextMediaPlayer")!=null && !playerReadyStatus.get("nextMediaPlayer")){
//			try {
//				Thread.sleep(1000);
//				count2 = count2 - 1;
//				if(count2<0){
//					break;
//				}
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		if(playerReadyStatus.get("nextMediaPlayer")!=null && playerReadyStatus.get("nextMediaPlayer")){
//			if(nextMediaPlayer!=null){
//				nextMediaPlayer.release();
//				nextMediaPlayer = null;
//			}
//		}
		try{
			destroyPlayers();
			playerReadyStatus.clear();
		}catch(Exception e){
			duoPlayerActivity.notifyError(400,e);
			CrashHandler.logErrorToFile(e);
		}
//		playersAreBeingStopping = false;
	}
	class PrepareVideoThread extends Thread{
		
		@Override
	    public void run() {
	        for (int i = (currentVideoIndex+1); (i < VideoListQueue.size() && i<(currentVideoIndex+2)); i++) {
		            nextMediaPlayer = new MediaPlayer();
		            
		            nextMediaPlayer
		                    .setAudioStreamType(AudioManager.STREAM_MUSIC);
	
		            nextMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
		    			@Override
		    			public void onPrepared(MediaPlayer mp) {
		    				hideLoading();
		    				playerReadyStatus.put("nextMediaPlayer", true);
		    			}
		    		});
		            
		            nextMediaPlayer.setOnInfoListener(new OnInfoListener() {
		    			@Override
		    			public boolean onInfo(MediaPlayer mp, int what, int extra) {
		    				
		    				if(what==MediaPlayer.MEDIA_INFO_BUFFERING_START){
		    					showLoading();
		    				}else 
		    				if(what==MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START){
		    					hideLoading();
		    				}
		    				return false;
		    			}
		    		});
		            nextMediaPlayer.setOnErrorListener(new OnErrorListener() {
						
						@Override
						public boolean onError(MediaPlayer mp, int what, int extra) {
							mp.reset();
							duoPlayerActivity.setVideoPlayingHintTest("播放时出现异常,请返回上级重新播放。");
							return false;
						}
					});
		            nextMediaPlayer
		                    .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
		                        @Override
		                        public void onCompletion(MediaPlayer mp) {
		                            onVideoPlayCompleted(mp);
		                        }
		                    });
	
		            try {
		                nextMediaPlayer.setDataSource(VideoListQueue.get(i));
		                nextMediaPlayer.prepare();
		                cachePlayer.setNextMediaPlayer(nextMediaPlayer);
		                //set new cachePlayer
		                cachePlayer = nextMediaPlayer;
		                //put nextMediaPlayer in cache
		                playersCache.put(String.valueOf(i), nextMediaPlayer);
		            } catch (IOException e) {
		                // TODO 自动生成的 catch 块
		                CrashHandler.logErrorToFile(e);
		            }
	        }

		}

	}

}
