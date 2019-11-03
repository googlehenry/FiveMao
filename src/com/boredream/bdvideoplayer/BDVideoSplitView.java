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
 * ��Ƶ������View
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
	
    private MediaPlayer firstPlayer,     //���𲥷Ž�����Ƶ���Ž����ĵ�һ����Ƶ
    nextMediaPlayer, //����һ����Ƶ���Ž����󣬲�����һ����Ƶ
    cachePlayer,     //����setNextMediaPlayer��player�������
    currentPlayer;   //����ǰ������Ƶ�����player����
    
    private Map<String,Boolean> playerReadyStatus = new HashMap<String,Boolean>();
//    private boolean playersAreBeingStopping = false;
    //���������Ƶ�˵�url
    private List<String> VideoListQueue = new ArrayList<String>();
    private List<String> urlsOriginal = new ArrayList<String>();
    //����player����Ļ���
    private HashMap<String, MediaPlayer> playersCache = new HashMap<String, MediaPlayer>();
    //��ǰ���ŵ�����Ƶ������
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
        surfaceHolder.addCallback(this); // ��Ϊ�����ʵ����SurfaceHolder.Callback�ӿڣ����Իص�����ֱ��this
        
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
            // ����Ѿ���ʼ���ڲ��ţ�����ͣͬʱ��¼״̬
            isBackgroundPause = true;
            mMediaPlayer.pause();
        }
    }

    public void onStart() {
        if (isBackgroundPause) {
            // ����л�����̨��ͣ�������л��������������
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
     * ��ʼ����
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
                    // �������ӵ������ֻ�����������״̬
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
        // TODO �Զ����ɵķ������

    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        //surfaceView������Ϻ����Ȼ�ȡ��ֱ����������Ƶ�ֶε�url
//        getVideoUrls();
        //Ȼ���ʼ�������ֶ���Ƶ��player����
//        initFirstPlayer();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        // TODO �Զ����ɵķ������

    }

    /*
     * ��ʼ�������׶���Ƶ��player
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
				duoPlayerActivity.setVideoPlayingHintTest("����ʱ�����쳣,�뷵���ϼ����²��š�");
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

        //����cachePlayerΪ��player����
        currentPlayer = firstPlayer;
        cachePlayer = firstPlayer;
        initNexttPlayer();
        
        //player�����ʼ����ɺ󣬿�������
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
     * �¿��̸߳����ʼ�����𲥷�ʣ����Ƶ�ֶε�player����,����UI�߳��������ʱ����
     */
    
    private void initNexttPlayer() {
    	prepareVideoThread = new PrepareVideoThread();
    	prepareVideoThread.start();
    }
    /*
     * ������һ����Ƶ���Ź����л�player������һ����Ƶ
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
	        	duoPlayerActivity.notifyError(200, new Exception("��Ӱ�������"));
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
//			duoPlayerActivity.setVideoPlayingHintTest("��̨����������׼����,���β���ʧ��,�����ϼ�ҳ�����ԡ�");
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
							duoPlayerActivity.setVideoPlayingHintTest("����ʱ�����쳣,�뷵���ϼ����²��š�");
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
		                // TODO �Զ����ɵ� catch ��
		                CrashHandler.logErrorToFile(e);
		            }
	        }

		}

	}

}
