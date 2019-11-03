package com.boredream.bdvideoplayer.view;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.ex.fivemao.exception.CrashHandler;

// TODO: 2017/6/16 ����̳�

/**
 * ��Ƶ����View��ע����� player.setAudioStreamType(AudioManager.STREAM_MUSIC);
 */
public class VideoBehaviorView extends FrameLayout implements GestureDetector.OnGestureListener {

    protected GestureDetector mGestureDetector;

    public static final int FINGER_BEHAVIOR_PROGRESS = 0x01;  //���ȵ���
    public static final int FINGER_BEHAVIOR_VOLUME = 0x02;  //��������
    public static final int FINGER_BEHAVIOR_BRIGHTNESS = 0x03;  //���ȵ���
    private int mFingerBehavior;
    private float mCurrentVolume; // ����������Χֵ�Ƚ�С ʹ��float����ʩ�����봦��.
    private int mMaxVolume;
    private int mCurrentBrightness, mMaxBrightness;

    protected Activity activity;
    protected AudioManager am;

    public VideoBehaviorView(Context context) {
        super(context);
        init();
    }

    public VideoBehaviorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoBehaviorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Context context = getContext();
        if (context instanceof Activity) {
            mGestureDetector = new GestureDetector(context.getApplicationContext(), this);
            activity = (Activity) context;
            am = (AudioManager) (context.getSystemService(Context.AUDIO_SERVICE));
        } else {
            throw new RuntimeException("VideoBehaviorView context must be Activity");
        }

        mMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mMaxBrightness = 255;
    }

    protected void endGesture(int behaviorType) {
        // sub
    }

    protected void updateSeekUI(int delProgress) {
        // sub
    }

    protected void updateVolumeUI(int max, int progress) {
        // sub
    }

    protected void updateLightUI(int max, int progress) {
        // sub
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
                endGesture(mFingerBehavior);
                break;
        }
        
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        //���� ��ָ��Ϊ
        mFingerBehavior = -1;
        mCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        try {
            mCurrentBrightness = (int) (activity.getWindow().getAttributes().screenBrightness * mMaxBrightness);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        final int width = getWidth();
        final int height = getHeight();
        if (width <= 0 || height <= 0) return false;

        /**
         * ����������ʼ2������� ������Ϊ. ��������:
         *  ��Ļ�з�Ϊ��X:
         *  1.������������Ϊ��Ƶ���ȵ���
         *  2.������������ ��������ȵ��� �������������.
         */
        if (mFingerBehavior < 0) {
            float moveX = e2.getX() - e1.getX();
            float moveY = e2.getY() - e1.getY();
            if (Math.abs(moveX) >= Math.abs(moveY))
                mFingerBehavior = FINGER_BEHAVIOR_PROGRESS;
            else if (e1.getX() <= width / 2) mFingerBehavior = FINGER_BEHAVIOR_BRIGHTNESS;
            else mFingerBehavior = FINGER_BEHAVIOR_VOLUME;
        }

        switch (mFingerBehavior) {
            case FINGER_BEHAVIOR_PROGRESS: { // ���ȱ仯
                // Ĭ�ϻ���һ����Ļ ��Ƶ�ƶ��˷���.
                int delProgress = (int) (1.0f * distanceX / width * 480 * 1000);
                // ���¿������
                updateSeekUI(delProgress);
                break;
            }
            case FINGER_BEHAVIOR_VOLUME: { // �����仯
                float progress = mMaxVolume * (distanceY / height) + mCurrentVolume;

                if (progress <= 1) progress = 1;
                if (progress >= mMaxVolume) progress = mMaxVolume;

                am.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(progress), 0);
                updateVolumeUI(mMaxVolume, Math.round(progress));
                mCurrentVolume = progress;
                break;
            }
            case FINGER_BEHAVIOR_BRIGHTNESS: { // ���ȱ仯
                try {
                    if (Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE)
                            == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                        Settings.System.putInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    }

                    int progress = (int) (mMaxBrightness * (distanceY / height) + mCurrentBrightness);

                    if (progress <= 20) progress = 20;
                    if (progress >= mMaxBrightness) progress = mMaxBrightness;

                    Window window = activity.getWindow();
                    WindowManager.LayoutParams params = window.getAttributes();
                    params.screenBrightness = progress / (float) mMaxBrightness;
                    window.setAttributes(params);

                    updateLightUI(mMaxBrightness, progress);

                    mCurrentBrightness = progress;
                } catch (Exception e) {
                    CrashHandler.logErrorToFile(e);
                }
                break;
            }
        }

        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

}
