package com.boredream.bdvideoplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.boredream.bdvideoplayer.utils.StringUtils;
import com.ex.fivemao.R;

/**
 * ����������˽��ȿ�
 */
public class VideoProgressOverlay extends FrameLayout {
    private ImageView mSeekIcon;
    private TextView mSeekCurProgress;
    private TextView mSeekDuration;

    private int mMaxBuffered = -1;
    private int mDuration = -1;
    private int mDelSeekDialogProgress = -1;
    private int mSeekDialogStartProgress = -1;
    
    boolean breakMaxBuffededProgress = false;

    public void setBreakMaxBuffededProgress(boolean breakF){
    	breakMaxBuffededProgress = breakF;
    }
    public VideoProgressOverlay(Context context) {
        super(context);
        init();
    }

    public VideoProgressOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoProgressOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.video_overlay_progress, this);

        mSeekIcon = (ImageView) findViewById(R.id.iv_seek_direction);
        mSeekCurProgress = (TextView) findViewById(R.id.tv_seek_current_progress);
        mSeekDuration = (TextView) findViewById(R.id.tv_seek_duration);
    }

    /**
     * ��ʾ���ȿ�
     *
     * @param delProgress ���ȱ仯ֵ
     * @param curPosition player��ǰ����
     * @param duration    player�ܳ���
     */
    public void show(int delProgress, int curPosition,int bufferedPercent, int duration) {
        if (duration <= 0) return;

        // ��ȡ��һ����ʾʱ�Ŀ�ʼ����
        if (mSeekDialogStartProgress == -1) {
            Log.i("DDD", "show: start seek = " + mSeekDialogStartProgress);
            mSeekDialogStartProgress = curPosition;
        }

        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
        }

        mDuration = duration;
        mMaxBuffered = duration*bufferedPercent/100;
        mDelSeekDialogProgress -= delProgress;
        int targetProgress = getTargetProgress();

        if (delProgress > 0) {
            // ����
            mSeekIcon.setImageResource(R.drawable.ic_video_back);
        } else {
            // ǰ��
            mSeekIcon.setImageResource(R.drawable.ic_video_speed);
        }
        mSeekCurProgress.setText(StringUtils.stringForTime(targetProgress));
        mSeekDuration.setText(StringUtils.stringForTime(mDuration));
    }

    /**
     * ��ȡ�����������Ŀ�����
     */
    public int getTargetProgress() {
        if (mDuration == -1) {
            return -1;
        }

        int newSeekProgress = mSeekDialogStartProgress + mDelSeekDialogProgress;
        if (newSeekProgress <= 0) newSeekProgress = 0;
        if (newSeekProgress >= mDuration) newSeekProgress = mDuration;
        
        if(!breakMaxBuffededProgress){
        	if (newSeekProgress >= mMaxBuffered) newSeekProgress = mMaxBuffered;
        }
        return newSeekProgress;
    }

    public void hide() {
        mDuration = -1;
        mSeekDialogStartProgress = -1;
        mDelSeekDialogProgress = -1;
        setVisibility(GONE);
    }

}
