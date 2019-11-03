package com.boredream.bdvideoplayer.listener;

import android.media.MediaPlayer;

/**
 * ��Ƶ�����ص����ǽ�ϵͳMediaPlayer�ĳ����ص���װ������
 */
public interface PlayerCallback {

	public boolean onInfo(MediaPlayer mp, int what, int extra);
    /**
     * ׼�����
     */
    void onPrepared(MediaPlayer mp);

    /**
     * ��Ƶsize�仯
     */
    void onVideoSizeChanged(MediaPlayer mp, int width, int height);

    /**
     * ������±仯
     *
     * @param percent ����ٷֱ�
     */
    void onBufferingUpdate(MediaPlayer mp, int percent);

    /**
     * �������
     */
    void onCompletion(MediaPlayer mp);

    /**
     * ��Ƶ����
     *
     * @param what  ��������
     *              <ul>
     *              <li>{@link android.media.MediaPlayer#MEDIA_ERROR_UNKNOWN}
     *              <li>{@link android.media.MediaPlayer#MEDIA_ERROR_SERVER_DIED}
     *              </ul>
     * @param extra ���������
     *              <ul>
     *              <li>{@link android.media.MediaPlayer#MEDIA_ERROR_IO}
     *              <li>{@link android.media.MediaPlayer#MEDIA_ERROR_MALFORMED}
     *              <li>{@link android.media.MediaPlayer#MEDIA_ERROR_UNSUPPORTED}
     *              <li>{@link android.media.MediaPlayer#MEDIA_ERROR_TIMED_OUT}
     *              <li><code>MEDIA_ERROR_SYSTEM (-2147483648)</code> - low-level system error.
     *              </ul>
     */
    void onError(MediaPlayer mp, int what, int extra);

    /**
     * ��Ƶ����״̬�仯
     *
     * @param isShow �Ƿ���ʾloading
     */
    void onLoadingChanged(boolean isShow);

    /**
     * ��Ƶ״̬�仯
     * <p><img src="../../../../../../images/mediaplayer_state_diagram.gif"
     * alt="MediaPlayer State diagram" border="0" /></p>
     *
     * @param curState ��ǰ��Ƶ״̬
     *                 <ul>
     *                 <li>{@link com.boredream.bdvideoplayer.BDVideoPlayer#STATE_ERROR}
     *                 <li>{@link com.boredream.bdvideoplayer.BDVideoPlayer#STATE_IDLE}
     *                 <li>{@link com.boredream.bdvideoplayer.BDVideoPlayer#STATE_PREPARING}
     *                 <li>{@link com.boredream.bdvideoplayer.BDVideoPlayer#STATE_PREPARED}
     *                 <li>{@link com.boredream.bdvideoplayer.BDVideoPlayer#STATE_PLAYING}
     *                 <li>{@link com.boredream.bdvideoplayer.BDVideoPlayer#STATE_PAUSED}
     *                 <li>{@link com.boredream.bdvideoplayer.BDVideoPlayer#STATE_PLAYBACK_COMPLETED}
     *                 </ul>
     */
    void onStateChanged(int curState);
}
