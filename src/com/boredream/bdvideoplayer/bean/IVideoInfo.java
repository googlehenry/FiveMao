package com.boredream.bdvideoplayer.bean;

import java.io.Serializable;

/**
 * ��Ƶ��������ʵ�ֱ��ӿ�
 */
public interface IVideoInfo extends Serializable {

    /**
     * ��Ƶ����
     */
    String getVideoTitle();

    /**
     * ��Ƶ����·�� url / file path
     */
    String getVideoPath();

}
