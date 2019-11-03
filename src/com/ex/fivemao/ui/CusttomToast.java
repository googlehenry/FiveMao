package com.ex.fivemao.ui;

import java.lang.reflect.Field;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ex.fivemao.R;
import com.ex.fivemao.exception.CrashHandler;


/**
 * 
 * 自定义Toast
 */
public class CusttomToast extends Toast {
    private static Toast mToast;

    public CusttomToast(Context context) {
        super(context);
    }

    public static Toast makeText(Context context, CharSequence text, int duration) {
        if (mToast == null) {
            mToast = new Toast(context);
        } else {
            mToast.cancel();
            mToast = new Toast(context);
        }

        //获取LayoutInflater对象
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        //获得屏幕的宽度
        int width = wm.getDefaultDisplay().getWidth();

        //由layout文件创建一个View对象
        View view = inflater.inflate(R.layout.layout_top_toast, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView toastTextView = (TextView) view.findViewById(R.id.tv_toast);
        //设置TextView的宽度为 屏幕宽度
        toastTextView.setLayoutParams(layoutParams);
        toastTextView.setText(text);

        mToast.setView(view);
        mToast.setGravity(Gravity.TOP, 0, 120);
        mToast.setDuration(duration);
        
        try {
            Object mTN = null;
            mTN = getField(mToast, "mTN");
            if (mTN != null) {
                Object mParams = getField(mTN, "mParams");
                if (mParams != null
                        && mParams instanceof WindowManager.LayoutParams) {
                    WindowManager.LayoutParams params = (WindowManager.LayoutParams) mParams;
                    params.windowAnimations = R.style.anim_view;
                    params.type = WindowManager.LayoutParams.TYPE_TOAST;  
                    params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON  
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
//                    params.gravity = Gravity.CENTER_HORIZONTAL|Gravity.TOP;
                }
            }
        } catch (Exception e) {
            CrashHandler.logErrorToFile(e);
        }
        return mToast;
    }

    /**
     * 反射字段
     *
     * @param object    要反射的对象
     * @param fieldName 要反射的字段名称
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static Object getField(Object object, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        if (field != null) {
            field.setAccessible(true);
            return field.get(object);
        }
        return null;
    }
}