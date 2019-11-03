package com.ex.fivemao.ui;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.ex.fivemao.R;
import com.ex.fivemao.exception.CrashHandler;

public class MakeSureDialog extends DialogFragment {  
    private View mView;  
    private TextView mTvContent;  
    private View tv_sure_divider; 
    private Button btn_dialog_X;
    private TextView tv_dialog_make_sure_title;
    private Map<String,String> params = new HashMap<String,String>();
    TextView mTvSure;  
    TextView mTvCancel;  
    private String content = "您确认吗？";  
    
    @Nullable  
    @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {  
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);  
        mView = inflater.inflate(R.layout.makesuredialog, container);
//        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.rgb(220, 220, 220)));  
        initView();  
        return mView;  
    }  
  
    
    public void setTextSizeContent(float sizeInPixel){
    	params.put("TextSizeContent", String.valueOf(sizeInPixel));
    }
   
    public void setTextTitle(String textTitle){
    	params.put("TextTitle", textTitle);
    }
    public void setDrawableLeft(String drawableLeftUrl){
    	params.put("DrawableLeft", drawableLeftUrl);
    }
    public void setTextSureButton(String sureText){
    	params.put("TextSureButton", sureText);
    }
    public void setTextCancelButton(String cancelText){
    	params.put("TextCancelButton", cancelText);
    }
    public String getTextContent(){
    	if(mTvContent!=null){
    		return mTvContent.getText().toString();
    	}
    	return "";
    }
    private void initView() {  
        mTvSure = (TextView) mView.findViewById(R.id.tv_sure);  
        mTvCancel = (TextView) mView.findViewById(R.id.tv_cancel);  
        mTvContent = (TextView) mView.findViewById(R.id.tv_dialog_make_sure_content);  
        tv_dialog_make_sure_title = (TextView) mView.findViewById(R.id.tv_dialog_make_sure_title);  
        tv_sure_divider = (View) mView.findViewById(R.id.tv_sure_divider);  
        btn_dialog_X = (Button)mView.findViewById(R.id.btn_dialog_X);
        mTvContent.setText(content);  
//        tv_sure_divider.setBackgroundColor(Color.CYAN);
        if(params.get("TextSizeContent")!=null){
        	mTvContent.setTextSize(Float.valueOf(params.get("TextSizeContent")));
        }
        if(params.get("TextSureButton")!=null){
        	mTvSure.setText(params.get("TextSureButton"));
        }
        if(params.get("TextCancelButton")!=null){
        	mTvCancel.setText(params.get("TextCancelButton"));
        }
        if(params.get("TextTitle")!=null){
        	tv_dialog_make_sure_title.setText(params.get("TextTitle"));
        }
        if(params.get("DrawableLeft")!=null){
        	String url = params.get("DrawableLeft");
        	Bitmap bitmap = loadNetImage(url);
        	Drawable image = new BitmapDrawable(bitmap);  
        	int minW = image.getMinimumWidth();
        	int minH = image.getMinimumHeight();
        	
        	
        	image.setBounds(0, 0, 200, 200*minH/minW);
        	mTvContent.setCompoundDrawables(image, null,null,null);
        }
        mTvSure.setOnClickListener(new View.OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                if (mListener != null) {  
                    mListener.onSureClick();  
                }  
                dismiss();  
            }  
        });  
        mTvCancel.setOnClickListener(new View.OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                if (mListener != null) {  
                    mListener.onCancelClick();  
                }  
                dismiss();  
            }  
        });  
        
        btn_dialog_X.setOnClickListener(new View.OnClickListener() {  
        	@Override  
        	public void onClick(View v) {  
        		mView.setVisibility(View.GONE);
        		dismiss();  
        	}  
        });  
        
    }  
  
    public void setContent(String content) {  
        this.content = content;  
  
    }  
  
    public interface onDialogClickListener {  
        public void onSureClick();  
  
        public void onCancelClick();  
    }  
  
    private onDialogClickListener mListener;  
  
    public void setDialogClickListener(onDialogClickListener mListener) {  
        this.mListener = mListener;  
    }  
    
    private Bitmap loadNetImage(String path){
    	Bitmap bitmap = null;
    	try{
	    	if(path.startsWith("data:image")){
	    		byte[] decodedString = Base64.decode(path.substring(path.indexOf("base64,")+"base64,".length()), Base64.DEFAULT);
	    		bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
	    	}else{
	            //把传过来的路径转成URL  
	            URL url = new URL(path);  
	            //获取连接  
	            HttpURLConnection connection = (HttpURLConnection) url.openConnection();  
	            //使用GET方法访问网络  
	            connection.setRequestMethod("GET");  
	            //超时时间为10秒  
	            connection.setConnectTimeout(10000);  
	            //获取返回码  
	            int code = connection.getResponseCode();  
	            if (code == 200) {  
	                InputStream inputStream = connection.getInputStream();  
	                //使用工厂把网络的输入流生产Bitmap  
	                bitmap = BitmapFactory.decodeStream(inputStream);  
	                inputStream.close();  
	            }else {  
	            }  
	    	}
	    	return bitmap;
    	}catch(Exception e){
    		return bitmap;
    	}
    }
   
    
    @Override  
    public void show(FragmentManager manager, String tag) {  
        try {  
        	//if(getActivity() != null && !getActivity().isFinishing()) {
        		super.show(manager, tag);  
        	//}
        } catch (IllegalStateException ignore) {  
        	CrashHandler.logErrorToFile(ignore);
            //  容错处理,不做操作  
        }  
    }
//    @Override
//    public void dismiss() {
//    	if(getActivity() != null && !getActivity().isFinishing()) {
//    		super.dismiss();
//    	}
//    }
//    @Override
//    public void dismissAllowingStateLoss() {
//    	if(getActivity() != null && !getActivity().isFinishing()) {
//    		super.dismissAllowingStateLoss();
//    	}
//    }
//   
}  