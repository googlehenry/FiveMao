package com.ex.fivemao.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.ex.fivemao.R;

public class SearchEditText extends EditText implements OnFocusChangeListener, OnKeyListener, TextWatcher{
private static final String TAG = "SearchEditText";
/**
* ͼ���Ƿ�Ĭ�������
*/
private boolean isIconLeft = false;
/**
* �Ƿ������������
*/
private boolean pressSearch = false;
/**
* ���������������
*/
private OnSearchClickListener listener;

private Drawable[] drawables; // �ؼ���ͼƬ��Դ
private Drawable drawableLeft, drawableDel; // ����ͼ���ɾ����ťͼ��
private int eventX, eventY; // ��¼�������
private Rect rect; // �ؼ�����

public void setOnSearchClickListener(OnSearchClickListener listener) {
this.listener = listener;
}

public interface OnSearchClickListener {
void onSearchClick(View view);
}

public SearchEditText(Context context) {
this(context, null);
init();
}


public SearchEditText(Context context, AttributeSet attrs) {
this(context, attrs, android.R.attr.editTextStyle);
init();
}


public SearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
super(context, attrs, defStyleAttr);
init();
}

private void init() {
setOnFocusChangeListener(this);
setOnKeyListener(this);
addTextChangedListener(this);
}


@Override
protected void onDraw(Canvas canvas) {
if (isIconLeft) { // �����Ĭ����ʽ��ֱ�ӻ���
if (length() < 1) {
drawableDel = null;
}
this.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, drawableDel, null);
super.onDraw(canvas);
} else { // �������Ĭ����ʽ����Ҫ��ͼ��������м�
if (drawables == null) drawables = getCompoundDrawables();
if (drawableLeft == null) drawableLeft = drawables[0];
float textWidth = getPaint().measureText(getHint().toString());
int drawablePadding = getCompoundDrawablePadding();
int drawableWidth = drawableLeft.getIntrinsicWidth();
float bodyWidth = textWidth + drawableWidth + drawablePadding;
canvas.translate((getWidth() - bodyWidth - getPaddingLeft() - getPaddingRight()) / 2, 0);
super.onDraw(canvas);
}
}


@Override
public void onFocusChange(View v, boolean hasFocus) {
// �����ʱ���ָ�Ĭ����ʽ
if (!pressSearch && TextUtils.isEmpty(getText().toString())) {
isIconLeft = hasFocus;
}
}

@Override
public boolean onKey(View v, int keyCode, KeyEvent event) {
pressSearch = (keyCode ==KeyEvent.KEYCODE_ENTER);
if (pressSearch && listener != null && event.getAction()==KeyEvent.ACTION_UP) {
/*���������*/
InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
if (imm.isActive()) {
imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
}
listener.onSearchClick(v);
}
return false;
}

@Override
public boolean onTouchEvent(MotionEvent event) {
// ���edit����
if (drawableDel != null && event.getAction() == MotionEvent.ACTION_UP) {
eventX = (int) event.getRawX();
            eventY = (int) event.getRawY();
            Log.i(TAG, "eventX = " + eventX + "; eventY = " + eventY);
            if (rect == null) rect = new Rect();
            getGlobalVisibleRect(rect);
            rect.left = rect.right - drawableDel.getIntrinsicWidth();
            if (rect.contains(eventX, eventY)) {
            setText("");
            }
}
// ɾ����ť������ʱ�ı�ͼ����ʽ
if (drawableDel != null && event.getAction() == MotionEvent.ACTION_DOWN) {
eventX = (int) event.getRawX();
            eventY = (int) event.getRawY();
            Log.i(TAG, "eventX = " + eventX + "; eventY = " + eventY);
            if (rect == null) rect = new Rect();
            getGlobalVisibleRect(rect);
            rect.left = rect.right - drawableDel.getIntrinsicWidth();
            if (rect.contains(eventX, eventY))
            drawableDel = this.getResources().getDrawable(R.drawable.edit_delete_pressed_icon);
} else {
drawableDel = this.getResources().getDrawable(R.drawable.edit_delete_icon);
}
return super.onTouchEvent(event);
}


@Override
public void afterTextChanged(Editable arg0) {
if (this.length() < 1) {
drawableDel = null;
} else {
drawableDel = this.getResources().getDrawable(R.drawable.edit_delete_icon);
}
}


@Override
public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
int arg3) {
}

@Override
public void onTextChanged(CharSequence arg0, int arg1, int arg2,
            int arg3) {
    }
}