package com.boredream.bdvideoplayer.listener;

public interface OnVideoControlListener {

    /**
     * ����
     */
    void onBack();

    /**
     * ȫ��
     */
    void onFullScreen();

    /**
     * ����������
     *
     * @param errorStatus ��ǰ����״̬
     *                    <ul>
     *                    <li>{@link com.boredream.bdvideoplayer.view.VideoErrorView#STATUS_NORMAL}
     *                    <li>{@link com.boredream.bdvideoplayer.view.VideoErrorView#STATUS_VIDEO_DETAIL_ERROR}
     *                    <li>{@link com.boredream.bdvideoplayer.view.VideoErrorView#STATUS_VIDEO_SRC_ERROR}
     *                    <li>{@link com.boredream.bdvideoplayer.view.VideoErrorView#STATUS_UN_WIFI_ERROR}
     *                    <li>{@link com.boredream.bdvideoplayer.view.VideoErrorView#STATUS_NO_NETWORK_ERROR}
     *                    </ul>
     */
    void onRetry(int errorStatus);

}
