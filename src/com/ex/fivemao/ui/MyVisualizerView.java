package com.ex.fivemao.ui;

import android.content.Context;  
import android.graphics.Canvas;  
import android.graphics.Color;  
import android.graphics.Paint;  
import android.graphics.Paint.Cap;  
import android.graphics.Paint.Join;  
import android.media.audiofx.Visualizer;  
import android.view.View;  
  
public class MyVisualizerView extends View implements Visualizer.OnDataCaptureListener {  
  
    private static final int DN_W = 470;//view����뵥����Ƶ��ռ�� - ����480 ��΢��  
    private static final int DN_H = 360;//view�߶��뵥����Ƶ��ռ��  
    private static final int DN_SL = 15;//������Ƶ����  
    private static final int DN_SW = 5;//������Ƶ��߶�  
  
    private int hgap = 0;
    private int vgap = 0;
    private int levelStep = 0;
    private float strokeWidth = 0;
    private float strokeLength = 0;
  
    protected final static int MAX_LEVEL = 30;//����������Ƶ�� - ������  
  
    protected final static int CYLINDER_NUM = 26;//������ - ������  
  
    protected Visualizer mVisualizer = null;//Ƶ����  
  
    protected Paint mPaint = null;//����  
  
    protected byte[] mData = new byte[CYLINDER_NUM];//������ ����  
  
    boolean mDataEn = true;  
  
    //���캯����ʼ������  
    public MyVisualizerView(Context context) {  
        super(context);  
  
        mPaint = new Paint();//��ʼ�����ʹ���  
        mPaint.setAntiAlias(true);//�����  
        mPaint.setColor(Color.WHITE);//������ɫ  
  
        mPaint.setStrokeJoin(Join.ROUND); //Ƶ��Բ��  
        mPaint.setStrokeCap(Cap.ROUND); //Ƶ��Բ��  
    }  
  
    //ִ�� Layout ����  
    @Override  
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {  
        super.onLayout(changed, left, top, right, bottom);  
  
        float w, h, xr, yr;  
  
        w = right - left;  
        h = bottom - top;  
        xr = w / (float) DN_W;  
        yr = h / (float) DN_H;  
  
        strokeWidth = DN_SW * yr;  
        strokeLength = DN_SL * xr;  
        hgap = (int) ((w - strokeLength * CYLINDER_NUM) / (CYLINDER_NUM + 1));  
        vgap = (int) (h / (MAX_LEVEL + 2));//Ƶ�׿�߶�  
  
        mPaint.setStrokeWidth(strokeWidth); //����Ƶ�׿���  
    }  
  
    //����Ƶ�׿�͵�Ӱ  
    protected void drawCylinder(Canvas canvas, float x, byte value) {  
        if (value == 0) {value = 1;}//������һ��Ƶ�׿�  
        for (int i = 0; i < value; i++) { //ÿ������������value��������  
            float y = (getHeight()/2 - i * vgap - vgap);//����y������  
            float y1=(getHeight()/2+i * vgap + vgap);  
            //����Ƶ�׿�  
            mPaint.setColor(Color.WHITE);//������ɫ  
            canvas.drawLine(x, y, (x + strokeLength), y, mPaint);//����Ƶ�׿�  
  
            //������������Ӱ  
            if (i <= 6 && value > 0) {  
                mPaint.setColor(Color.WHITE);//������ɫ  
                mPaint.setAlpha(100 - (100 / 6 * i));//��Ӱ��ɫ  
                canvas.drawLine(x, y1, (x + strokeLength), y1, mPaint);//����Ƶ�׿�  
            }  
        }  
    }  
  
    @Override  
    public void onDraw(Canvas canvas) {  
        int j=-4;  
        for (int i = 0; i < CYLINDER_NUM/2-4; i++) { //����25��������  
              
            drawCylinder(canvas, strokeWidth / 2 + hgap + i * (hgap + strokeLength), mData[i]);  
        }  
        for(int i =CYLINDER_NUM; i>=CYLINDER_NUM/2-4; i--){  
        j++;  
            drawCylinder(canvas, strokeWidth / 2 + hgap + (CYLINDER_NUM/2+j-1 )* (hgap + strokeLength), mData[i-1]);  
        }  
    }  
  
    /** 
     * It sets the visualizer of the view. DO set the viaulizer to null when exit the program. 
     * 
     * @parma visualizer It is the visualizer to set. 
     */  
    public void setVisualizer(Visualizer visualizer) {  
        if (visualizer != null) {  
            if (!visualizer.getEnabled()) {  
                visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);  
            }  
            levelStep = 230 / MAX_LEVEL;  
            visualizer.setDataCaptureListener(this, Visualizer.getMaxCaptureRate() / 2, false, true);  
  
        } else {  
  
            if (mVisualizer != null) {  
                mVisualizer.setEnabled(false);  
                mVisualizer.release();  
            }  
        }  
        mVisualizer = visualizer;  
    }  
  
    //����ص�Ӧ�òɼ����ǿ��ٸ���Ҷ�任�йص�����  
    @Override  
    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {  
        byte[] model = new byte[fft.length / 2 + 1];  
        if (mDataEn) {  
            model[0] = (byte) Math.abs(fft[1]);  
            int j = 1;  
            for (int i = 2; i < fft.length; ) {  
                model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);  
                i += 2;  
                j++;  
            }  
        } else {  
            for (int i = 0; i < CYLINDER_NUM; i++) {  
                model[i] = 0;  
            }  
        }  
        for (int i = 0; i < CYLINDER_NUM; i++) {  
            final byte a = (byte) (Math.abs(model[CYLINDER_NUM - i]) / levelStep);  
  
            final byte b = mData[i];  
            if (a > b) {  
                mData[i] = a;  
            } else {  
                if (b > 0) {  
                    mData[i]--;  
                }  
            }  
        }  
        postInvalidate();//ˢ�½���  
    }  
  
    //����ص�Ӧ�òɼ����ǲ�������  
    @Override  
    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {  
        // Do nothing...  
    }  
}  