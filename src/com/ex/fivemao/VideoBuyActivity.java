package com.ex.fivemao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ex.fivemao.exception.CrashHandler;
import com.ex.fivemao.io.CacheFileUtil;
import com.ex.fivemao.io.PropertiesUtilWithEnc;
import com.ex.fivemao.io.WithEnc;
import com.ex.fivemao.ui.CusttomToast;
import com.ex.fivemao.ui.MakeSureDialog;
import com.ex.fivemao.utils.NetworkUtil;
import com.example.myvideos.http.HttpsGetThread;
import com.example.myvideos.http.MultipartFormHttpsPostThread;
import com.example.myvideos.http.proxy.Utils;


public class VideoBuyActivity extends BaseActivity {
	public static String existingAliTransaction;
	public static String videoID ;
	public static String videoTitle ;
	public static String videoCoverpageImageLink ;
	public static String videoDescription ;
	public static String videoFileLink ;
	private static int IMAGE_GALLARY_CODE = 2;
	private static int IMAGE_CUT_CODE = 3;
	public static String videoType;
	public static String minimumAge;
	
	
	Button paymentCallAli;
	Button paymentEvidence;
	Button paymentEvidenceImage;
	
	ImageView paymentEvidenceImagePreview;
	ImageView imgPaymentAccountImage;
	ImageView imgPaymentEvidenceImage;
	EditText aliOrderCode;
	TextView buyVideoName;
	TextView processingAliTransLoadingHint;
	boolean isProcessScreenshot = false;
	AlertDialog blockUserDialog = null;
	String myScreenshotFolderPath ;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_videobuy);
        
        paymentCallAli = (Button)findViewById(R.id.paymentCallAli);
        paymentEvidence = (Button)findViewById(R.id.paymentEvidence);
        paymentEvidenceImage = (Button)findViewById(R.id.paymentEvidenceImage);
        aliOrderCode = (EditText)findViewById(R.id.aliOrderCode);
        buyVideoName = (TextView)findViewById(R.id.buyVideoName);
        processingAliTransLoadingHint = (TextView)findViewById(R.id.processingAliTransLoadingHint);
        paymentEvidenceImagePreview = (ImageView)findViewById(R.id.paymentEvidenceImagePreview);
        imgPaymentAccountImage = (ImageView)findViewById(R.id.imgPaymentAccountImage);
        imgPaymentEvidenceImage = (ImageView)findViewById(R.id.imgPaymentEvidenceImage);
        
        //use encoded image
        imgPaymentAccountImage.setImageBitmap(WithEnc.getImageBigMapWithEnc(getResources().openRawResource(R.drawable.post_pay_alipay)));
        imgPaymentEvidenceImage.setImageBitmap(WithEnc.getImageBigMapWithEnc(getResources().openRawResource(R.drawable.post_pay_ali_feedback)));
        
        
//        if(MeActivity.loginUserName!=null){
//        	setTitle(getTitle()+">>"+MeActivity.loginUserName+"�ѵ�½");
//        }else{
//        	setTitle(getTitle()+">>"+"δ��½�û�");
//        }
        
        Intent intent = getIntent();
        videoID = intent.getStringExtra("id");
        videoTitle = intent.getStringExtra("title");
        videoDescription = intent.getStringExtra("description");
        videoCoverpageImageLink = intent.getStringExtra("videoCoverpageImageLink");
        videoFileLink = intent.getStringExtra("videoFileLink");
        videoType = intent.getStringExtra("videoType");
        minimumAge = intent.getStringExtra("minimumAge");
        
        buyVideoName.setText("����:"+videoTitle);
        
        if(videoCoverpageImageLink!=null && videoCoverpageImageLink.length()>0 && !videoCoverpageImageLink.contains("null")){
        	Bitmap bitmap = NetworkUtil.loadNetImage(videoCoverpageImageLink);
        	Drawable image = new BitmapDrawable(bitmap);  
        	int minW = image.getMinimumWidth();
        	int minH = image.getMinimumHeight();
        	image.setBounds(0, 0, minW, minH);
        	buyVideoName.setCompoundDrawables(null, null, null, image);
        }
        
        paymentEvidenceImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				uploadScreenshotClicked();
				
			}
		});
        
        paymentCallAli.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(loginUserName==null || loginUserName.length()<=2){
					MakeSureDialog dialog = new MakeSureDialog();  
			        dialog.setContent("����û�е�¼,���ȵ�¼...");  
			        dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
			            @Override  
			            public void onSureClick() {
			            	gotoMePage();
			            }  
			            @Override  
			            public void onCancelClick() {  }  
			        });  
			        try{
	   	          		dialog.show(getFragmentManager(),"");
	   	          	}catch(Exception e1){
	   	          		CrashHandler.logErrorToFile(e1);
	   	          	}
				}else{
					
					MakeSureDialog dialog = new MakeSureDialog();  
      	          	 
      	          	 dialog.setTextTitle("֧����ʾ");
      	          	dialog.setTextSizeContent(20);
      	          	 dialog.setContent("����\"ֱ��֧��\"�����Ȳ鿴\"����ָ��\"����������ʱ�����Ͻǲ˵�-����ָ���в鿴��");
      	          	dialog.setTextSureButton("ֱ��֧��");
      	          	dialog.setTextCancelButton("�ȿ�ָ��");
      	          	dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
      	          	   @Override
      	          	   public void onSureClick() {
      	          		   callAlipayReceiveMoney();
      	          	   }  
      	          	   @Override  
      	          	   public void onCancelClick() {
      	          		   gotoHelpGuideActivity();
      	          	   }  
      	          	});  
      	          	try{
      	          		dialog.setCancelable(false);
	   	          		dialog.show(getFragmentManager(),"");
	   	          	}catch(Exception e1){
	   	          		CrashHandler.logErrorToFile(e1);
	   	          	}
      	          	
					
				}
			    
			}
		});
        
        //CusttomToast.makeText(this, "���Ͻǲ˵�-�鿴\"����ָ��\"", Toast.LENGTH_LONG).show();
    }




	@Override
    protected void onResume() {
    	super.onResume();
//    	if(VideoBuyActivity.existingAliTransaction==null){
    	if(!isProcessScreenshot){
        	checkIfProcessingAlipayTransaction();
        	if(VideoBuyActivity.existingAliTransaction!=null){
        		paymentEvidenceImage.setFocusable(true);
        		paymentEvidenceImage.setFocusableInTouchMode(true);
        		paymentEvidenceImage.requestFocus();
        	}
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        
        return super.onOptionsItemSelected(item);
    }
    
    private void addNewFilmAuth() {  
        
        HttpsGetThread thread = new HttpsGetThread(new Handler() { 
            @Override  
            public void handleMessage(Message msg) {  
                super.handleMessage(msg);  
                String result = (String) msg.obj;  
                //etShowInfo.setText(result+":"+url);
                switch (msg.what) {  
                case 200:  
                	try {
                		if(result==null || result.length()==0){
					        processingAliTransLoadingHint.setVisibility(View.GONE);
					        isProcessScreenshot = false;
                		}else{
                			JSONObject json = new JSONObject(result);
    						String status = json.getString("status");
    						String message = json.getString("message");
    						//Toast.makeText( VideoBuyActivity.this, status+":"+message, Toast.LENGTH_LONG).show();
    						if(status!=null && status.contains("Fail")){
    							//Toast.makeText( VideoBuyActivity.this, result+",��ȷ�϶�������,�Ժ����ԡ�", Toast.LENGTH_LONG).show();
    							MakeSureDialog dialog = new MakeSureDialog();  
    							if(videoCoverpageImageLink!=null && videoCoverpageImageLink.length()>0 && !videoCoverpageImageLink.contains("null")){
    								dialog.setDrawableLeft(videoCoverpageImageLink);
    							}
    					        dialog.setContent("���Ķ���ƾ֤����,��ȷ���Ѿ����֧��,��ȡ��ȷ�������µĶ�����ͼ,��Ҫ�ظ��ϴ��ɽ�ͼ,�Ժ����ԡ������Ͻ���ϵ�ͷ���");  
    					        dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
    					            @Override  
    					            public void onSureClick() {  
    					            	if(blockUserDialog!=null){
						            		blockUserDialog.dismiss();
						            	}
    					            	isProcessScreenshot = false;
    					            }  
    					  
    					            @Override  
    					            public void onCancelClick() {  
    					            	if(blockUserDialog!=null){
						            		blockUserDialog.dismiss();
						            	}
    					            	isProcessScreenshot = false;
    					//������ȡ������  
    					            }  
    					        });  
    					        try{
    			   	          		dialog.show(getFragmentManager(),"");
    			   	          	}catch(Exception e1){
    			   	          		CrashHandler.logErrorToFile(e1);
    			   	          	} 
    					        
    						}else if(status!=null && status.contains("Success")){
    							
    							CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(),
    									"alipay_trans.dat", "PaymentCompleted");
    							VideoBuyActivity.existingAliTransaction = null;
    							
    							//Toast.makeText( VideoBuyActivity.this, "������һҳ�����µ�����Ÿõ�Ӱ", Toast.LENGTH_LONG).show();
    							MakeSureDialog dialog = new MakeSureDialog();  
    							if(videoCoverpageImageLink!=null && videoCoverpageImageLink.length()>0 && !videoCoverpageImageLink.contains("null")){
    								dialog.setDrawableLeft(videoCoverpageImageLink);
    							}
    					        dialog.setContent("��"+videoTitle+"������ɹ�������ȥ�ۿ�?");  
    					        dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
    					            @Override  
    					            public void onSureClick() {
    					            	saveScreenshotsDirectory();
    					            	if(blockUserDialog!=null){
						            		blockUserDialog.dismiss();
						            	}
    					            	isProcessScreenshot = false;
    					            	onBackPressed();
//    					            	Intent intent=new Intent();
//    	    				            intent.setClass(VideoBuyActivity.this, MainActivity.class);  
//    	    				            startActivity(intent);
    					            }
    					  
    					            @Override  
    					            public void onCancelClick() {  
    					            	if(blockUserDialog!=null){
						            		blockUserDialog.dismiss();
						            	}
    					            	isProcessScreenshot = false;
    					//������ȡ������  
    					            }  
    					        });  
    					        try{
    			   	          		dialog.show(getFragmentManager(),"");
    			   	          		broadCastMessageToBoard("[����]"+starMyName(loginUserName)+"��"+new SimpleDateFormat("yyyy-MM-dd HH:mm:SS").format(new Date())+"������ӰƬ��"+VideoBuyActivity.videoTitle+"��");
    			   	          	}catch(Exception e1){
    			   	          		CrashHandler.logErrorToFile(e1);
    			   	          	}
    					        
    						}
					        processingAliTransLoadingHint.setVisibility(View.GONE);
                		}
						
					} catch (JSONException e) {
						CrashHandler.logErrorToFile(e);
				        processingAliTransLoadingHint.setVisibility(View.GONE);
				        isProcessScreenshot = false;
					}
                    break;  
                case 404:  
                    // ����ʧ��  
                    Log.e("TAG", "����ʧ��!");  
                    processingAliTransLoadingHint.setVisibility(View.GONE);
                    isProcessScreenshot = false;
                    break;  
                }  
  
            }

        },String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/user/json/addNewFilmAuth?uname="+loginUserName+"&filmID="+videoID+"&ordercode="+aliOrderCode.getText().toString().trim()+"&resType=Film", 200);  
        thread.start();
    }
   
    private void saveScreenshotsDirectory(){
    	if(myScreenshotFolderPath!=null){
    		CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "screenshot.default.dir.dat",myScreenshotFolderPath);
    	}
    }
    
    private String getScreenshotsDirectory(){
    	String path = CacheFileUtil.readCacheFile(getCacheDir().getAbsolutePath(), "screenshot.default.dir.dat");
    	return path;
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	if(requestCode==IMAGE_GALLARY_CODE){
    		if(data!=null){
    			Uri uri = data.getData();
    			try {
    				showBlockUserDialog(); 
					
			        
					Bitmap imageData = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
					paymentEvidenceImagePreview.setImageBitmap(imageData);
					String realPath = CacheFileUtil.getRealFilePath(getApplicationContext(), uri);
					
					sendScreenShotToServ(realPath);
					
					Toast.makeText( VideoBuyActivity.this, "���ڴ���...", Toast.LENGTH_LONG).show();
				} catch (FileNotFoundException e) {
					Toast.makeText( VideoBuyActivity.this, "ͼƬû�ҵ�,������ѡ��", Toast.LENGTH_LONG).show();
					processingAliTransLoadingHint.setVisibility(View.GONE);
					isProcessScreenshot = false;
				} catch (IOException e) {
					Toast.makeText( VideoBuyActivity.this, "ͼƬ��ȡ����,������ѡ��", Toast.LENGTH_LONG).show();
					processingAliTransLoadingHint.setVisibility(View.GONE);
					isProcessScreenshot = false;
				} catch (Exception e) {
					Toast.makeText( VideoBuyActivity.this, "ͼƬ�ϴ�ʧ��,�������ύ��"+e.getMessage(), Toast.LENGTH_LONG).show();
					processingAliTransLoadingHint.setVisibility(View.GONE);
					isProcessScreenshot = false;
				}
    			
//    			chop(uri);
    		}
    	}else if(requestCode==IMAGE_CUT_CODE){
    		if(data!=null){
    			Bitmap imageData = data.getParcelableExtra("data");
    			paymentEvidenceImagePreview.setImageBitmap(imageData);
    		}
    	}
    }


	private void showBlockUserDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);  
		builder.setIcon(android.R.drawable.ic_dialog_info);  
		builder.setTitle("ϵͳ���ڴ����û�����,������������ٶ�,���Ե�20��");  
		builder.setCancelable(false);
		blockUserDialog = builder.show();
	}


	private void sendScreenShotToServ(String realPath) {
		Map<String,String> params = new HashMap<String,String>();
		Map<String,File> files = new HashMap<String,File>();
		params.put("userName", loginUserName);
		files.put("imageFile", new File(realPath));
		
		new MultipartFormHttpsPostThread(new Handler(){
			private boolean validCode(String message){
				String msg = message.replace("\n\n", "").replace(" ", "").replace("O", "0").replace("_", "").replace("[)", "");
				String[] lines = msg.split("\n");
				boolean foundAccoutTo = false;
				boolean foundOrderCode = false;
				String foundOrderCodeStr = "";
				
				for(String line:lines){
					if(line.contains("136******45")||line.contains("pre***@yahoo")||line.contains("pre***@yaboo")){
						foundAccoutTo = true;
					}
					
					
					Pattern pattern = Pattern.compile("[\\da-zA-Z\\s\\[)]{24,}");
					Matcher matcher = pattern.matcher(line);
					
					while(matcher.find()){
						String match = matcher.group();
						foundOrderCode = true;
						foundOrderCodeStr = match;
					}
				}
				
				
				boolean rs = (foundAccoutTo&&foundOrderCode);

				aliOrderCode.setText(foundOrderCodeStr);
				
				return rs;
			}
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				
				String result = (String) msg.obj;  
				//aliOrderCode.setVisibility(View.VISIBLE);
				
				try{
				
					JSONObject json = new JSONObject(result);
					String status = json.getString("status");
					String message = json.getString("message");
					
					
					if(status!=null && status.contains("Success") && validCode(message)){
						processCode();
					}else{
						MakeSureDialog dialog = new MakeSureDialog();  
						if(videoCoverpageImageLink!=null && videoCoverpageImageLink.length()>0 && !videoCoverpageImageLink.contains("null")){
							dialog.setDrawableLeft(videoCoverpageImageLink);
						}
				        dialog.setContent("����ƾ֤����,ʧ��ԭ��:ʹ���˾ɽ�ͼ,�ظ��ύ,����Ť��(��������,����,�༭)�ȡ������³���,�����Ͻ���ϵ�ͷ���");  
				        dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
				            @Override  
				            public void onSureClick() { 
				            	if(blockUserDialog!=null){
				            		blockUserDialog.dismiss();
				            	}
				            	processingAliTransLoadingHint.setVisibility(View.GONE);
				            	isProcessScreenshot = false;
				            }  
				            @Override  
				            public void onCancelClick() { 
				            	if(blockUserDialog!=null){
				            		blockUserDialog.dismiss();
				            	}
				            	processingAliTransLoadingHint.setVisibility(View.GONE);
				            	isProcessScreenshot = false;
				            }  
				        });  
				        try{
		   	          		dialog.show(getFragmentManager(),"");
		   	          	}catch(Exception e1){
		   	          		CrashHandler.logErrorToFile(e1);
		   	          	}
				        processingAliTransLoadingHint.setVisibility(View.GONE);
					}
				}catch(Exception e){
					Toast.makeText( VideoBuyActivity.this, "��������������������Ժ����ԡ�", Toast.LENGTH_LONG).show();
					processingAliTransLoadingHint.setVisibility(View.GONE);
					isProcessScreenshot = false;
				}
			}
		},String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/ocr/parseImage", params, files)
		.start();
	};
    
    private void chop(Uri imageUri){
    	WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
    	DisplayMetrics outMetrics = new DisplayMetrics();
    	wm.getDefaultDisplay().getMetrics(outMetrics);
    	int screenX = outMetrics.widthPixels;
    	int screenY = outMetrics.heightPixels;
    	
    	Intent intent = new Intent("com.android.camera.action.CROP");
    	intent.setDataAndType(imageUri, "image/*");
    	intent.putExtra("crop", "true");
    	intent.putExtra("aspectX", 1);
    	intent.putExtra("aspectY", 1.5);
    	intent.putExtra("outputX", screenX);
    	intent.putExtra("outputY", screenY);
    	intent.putExtra("outputFormat", "JPEG");
    	intent.putExtra("noFaceDetection",true);
    	intent.putExtra("return-data", true);
    	
    	startActivityForResult(intent, IMAGE_CUT_CODE);
    }
    
    private void processCode(){
    	
			if(aliOrderCode.getText().length()>=24){
				String dateString = aliOrderCode.getText().toString().substring(0, 8);
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
					Date userDatefromOrder = sdf.parse(dateString);
					long hoursDiff = (new Date().getTime()-userDatefromOrder.getTime())/1000/60/60;
					if(hoursDiff>=0 && hoursDiff<=36){
						addNewFilmAuth();
					}else{
						Toast.makeText( VideoBuyActivity.this, "���Ķ������벻��"+hoursDiff+","+dateString, Toast.LENGTH_LONG).show();
					}
					
				} catch (ParseException e) {
					CrashHandler.logErrorToFile(e);
				}
				
				
			}else{
				Toast.makeText( VideoBuyActivity.this, "��������ȷ�Ķ�������", Toast.LENGTH_LONG).show();
			}
    }
    

    

    private boolean checkIfProcessingAlipayTransaction() {
    	String content = CacheFileUtil.readCacheFile(getCacheDir().getAbsolutePath(), "alipay_trans.dat");
    	if(content!=null && content.length()>0 && content.contains("notakid")){
    		final String[] alipayObjs = content.split("notakid");
    		if(alipayObjs.length>=6 && loginUserName!=null && loginUserName.equals(alipayObjs[5])){
	            
    			VideoBuyActivity.existingAliTransaction = alipayObjs[0]; 
                VideoBuyActivity.videoID =alipayObjs[1];
                VideoBuyActivity.videoTitle=alipayObjs[2];
                VideoBuyActivity.videoFileLink=alipayObjs[3];
                VideoBuyActivity.videoDescription=alipayObjs[4];
                
	            MakeSureDialog dialog = new MakeSureDialog();  
	          	 
	          	 dialog.setTextTitle("���������:�ϴ�֧����ͼ?");
	          	 dialog.setTextSureButton("�ϴ���ͼ");
	          	 dialog.setTextCancelButton("��ν�ͼ");
	          	 dialog.setContent("����ʱ��:"+alipayObjs[0]+"\n�����Ӱ����"+alipayObjs[2]+"��\n\n*��ȡ��ͼ:֧����>>�ҵ�>>�˵�>>�����굥>>����,Ȼ����'�ϴ���ͼ'");
	          	 
	          	dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
	          	   @Override
	          	   public void onSureClick() {
	          		   uploadScreenshotClicked();
	          	   }  
	          	   @Override  
	          	   public void onCancelClick() {  
	          		 gotoHelpGuideActivity();
	          	   }  
	          	});  
	          	//dialog.setCancelable(false);
	          	try{
   	          		dialog.show(getFragmentManager(),"");
   	          	}catch(Exception e1){
   	          		CrashHandler.logErrorToFile(e1);
   	          	}
	            return true;
    		}
    	}
    	return false;
		
	}
    @Override
    public void onBackPressed() {
    	existingAliTransaction=null;
    	videoID =null;
    	videoTitle=null ;
    	videoDescription=null ;
    	videoFileLink =null;
    	super.onBackPressed();
    }


	private void uploadScreenshotClicked() {
		if(loginUserName==null || loginUserName.length()<=2){
			MakeSureDialog dialog = new MakeSureDialog();  
		    dialog.setContent("����û�е�¼,���ȵ�¼...");  
		    dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
		        @Override  
		        public void onSureClick() {
		        	gotoMePage();
		        }  
		        @Override  
		        public void onCancelClick() {  }  
		    });  
		    	try{
	          		dialog.show(getFragmentManager(),"");
	          	}catch(Exception e1){
	          		CrashHandler.logErrorToFile(e1);
	          	}
			
		}else{
			
			myScreenshotFolderPath = getScreenshotsDirectory();
			if(myScreenshotFolderPath==null || myScreenshotFolderPath.trim().length()==0){
				myScreenshotFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DCIM/Screenshots";
			}
			
			List<File> screenshots = Utils.listTop1ScreenshotsIfAvailable(VideoBuyActivity.this,
					new File(myScreenshotFolderPath));
			
			if(screenshots!=null && screenshots.size()>0){
				final File screenshotRecommended = screenshots.get(screenshots.size()-1);
				
				
				final Dialog dia = new Dialog(VideoBuyActivity.this);
				
		        try {
		        	dia.setTitle("�ϴ������֧����ͼ��");
		        	dia.setContentView(R.layout.dialog_showrecscreenshot);
		        	ImageView imageView = (ImageView) dia.findViewById(R.id.imgShowRecScrenshot);
		        	Button btnPositiveShowRecImg = (Button) dia.findViewById(R.id.btnPositiveShowRecImg);
		        	Button btnNegativeShowRecImg = (Button) dia.findViewById(R.id.btnNegativeShowRecImg);
					imageView.setImageBitmap(WithEnc.getImageBigMapNoEnc(
							new FileInputStream(screenshotRecommended)));
					
					btnPositiveShowRecImg.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							dia.dismiss();
							isProcessScreenshot = true;
							processingAliTransLoadingHint.setVisibility(View.VISIBLE);
							showBlockUserDialog(); 
							sendScreenShotToServ(screenshotRecommended.getAbsolutePath());
						}
					});
					btnNegativeShowRecImg.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							dia.dismiss();
							askUserToPickScreenshot();
						}
					});
					
					dia.show();
				} catch (Throwable e) {
					CrashHandler.logErrorToFile(e);
				}
		        

				
				
			}else{
				askUserToPickScreenshot();
			}
			
		}
	}


	private void askUserToPickScreenshot() {
		isProcessScreenshot = true;
		processingAliTransLoadingHint.setVisibility(View.VISIBLE);
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, IMAGE_GALLARY_CODE);
	}




	private void callAlipayReceiveMoney() {
		paymentEvidenceImage.setFocusable(true);
		paymentEvidenceImage.setFocusableInTouchMode(true);
		paymentEvidenceImage.requestFocus();
		    
		String transID = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS").format(new Date());
		
		CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(),
				"alipay_trans.dat", transID+"notakid"+videoID+"notakid"+videoTitle+"notakid"+videoFileLink+"notakid"+videoDescription+"notakid"+loginUserName);
		
		Intent intent= new Intent();        
		intent.setAction("android.intent.action.VIEW");    
		Uri content_url = Uri.parse(PropertiesUtilWithEnc.getString("paymentAliUrl"));   
		intent.setData(content_url);  
		startActivity(intent);
	}
    
}
