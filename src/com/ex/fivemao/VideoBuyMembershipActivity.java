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
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.example.myvideos.http.HttpsGetThread;
import com.example.myvideos.http.MultipartFormHttpsPostThread;
import com.example.myvideos.http.proxy.Utils;


public class VideoBuyMembershipActivity extends BaseActivity {
	public static String existingAliTransaction;
	public static Integer membershipdays = 1;
	private static int IMAGE_GALLARY_CODE = 2;
	private static int IMAGE_CUT_CODE = 3;
	
	Button paymentCallAli;
	Button paymentEvidence;
	Button paymentEvidenceImage;
	ImageView paymentEvidenceImagePreview;
	ImageView imgPaymentAccountImage;
	ImageView imgPaymentEvidenceImage;
	EditText aliOrderCode;
	TextView buyVideoName;
	TextView buyVideoIntro;
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
        buyVideoIntro = (TextView)findViewById(R.id.buyVideoIntro);
        processingAliTransLoadingHint = (TextView)findViewById(R.id.processingAliTransLoadingHint);
        paymentEvidenceImagePreview = (ImageView)findViewById(R.id.paymentEvidenceImagePreview);
        imgPaymentAccountImage = (ImageView)findViewById(R.id.imgPaymentAccountImage);
        imgPaymentEvidenceImage = (ImageView)findViewById(R.id.imgPaymentEvidenceImage);
        
        Intent intent = getIntent();
        if(intent!=null){
        	membershipdays = intent.getIntExtra("membershipdays", 1);
        }
        if(membershipdays==1){
        	buyVideoName.setText("����"+membershipdays+"�ջ�Ա/1.0Ԫ");
        	buyVideoIntro.setText(buyVideoIntro.getText().toString().replace("0.5Ԫ", "1.0Ԫ"));
        	imgPaymentAccountImage.setImageBitmap(WithEnc.getImageBigMapWithEnc(getResources().openRawResource(R.drawable.post_pay_alipay_membership)));
        }else if(membershipdays==31){
        	buyVideoName.setText("����һ��("+membershipdays+"��)��Ա/8.0Ԫ");
        	buyVideoIntro.setText(buyVideoIntro.getText().toString().replace("0.5Ԫ", "8.0Ԫ"));
        	imgPaymentAccountImage.setImageBitmap(WithEnc.getImageBigMapWithEnc(getResources().openRawResource(R.drawable.post_pay_alipay_membership_1mon)));
        }else if(membershipdays==93){
        	buyVideoName.setText("��������("+membershipdays+"��)��Ա/18.0Ԫ");
        	buyVideoIntro.setText(buyVideoIntro.getText().toString().replace("0.5Ԫ", "18.0Ԫ"));
        	imgPaymentAccountImage.setImageBitmap(WithEnc.getImageBigMapWithEnc(getResources().openRawResource(R.drawable.post_pay_alipay_membership_3mon)));
        }
        
        //use encoded image
        imgPaymentEvidenceImage.setImageBitmap(WithEnc.getImageBigMapWithEnc(getResources().openRawResource(R.drawable.post_pay_ali_feedback)));
        
        
        
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

    private void callAlipayReceiveMoney() {
    	paymentEvidenceImage.setFocusable(true);
	    paymentEvidenceImage.setFocusableInTouchMode(true);
	    paymentEvidenceImage.requestFocus();
	        
		String transID = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS").format(new Date());
		
		CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(),
				"alipay_trans_membership.dat", transID+"notakid"+membershipdays+"notakid"+loginUserName);
		
		Intent intent= new Intent();
	    intent.setAction("android.intent.action.VIEW");    
	    if(membershipdays==1){
	    	Uri content_url = Uri.parse(PropertiesUtilWithEnc.getString("paymentMembershipAliUrl"));   
		    intent.setData(content_url);  
        }else if(membershipdays==31){
        	Uri content_url = Uri.parse(PropertiesUtilWithEnc.getString("paymentMembershipAliUrl1Mon"));   
		    intent.setData(content_url);  
        }else if(membershipdays==93){
        	Uri content_url = Uri.parse(PropertiesUtilWithEnc.getString("paymentMembershipAliUrl3Mon"));   
		    intent.setData(content_url);  
        }
	    
	    startActivity(intent);
	}
    @Override
    protected void onResume() {
    	super.onResume();
//    	if(VideoBuyActivity.existingAliTransaction==null){
    	if(!isProcessScreenshot){
        	checkIfProcessingAlipayTransaction();
        	if(VideoBuyMembershipActivity.existingAliTransaction!=null){
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
    									"alipay_trans_membership.dat", "PaymentCompleted");
    							VideoBuyMembershipActivity.existingAliTransaction = null;
    							
    							//Toast.makeText( VideoBuyActivity.this, "������һҳ�����µ�����Ÿõ�Ӱ", Toast.LENGTH_LONG).show();
    							MakeSureDialog dialog = new MakeSureDialog();  
    					        dialog.setContent(membershipdays+"�ջ�Ա����ɹ���������һ������?");  
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
//    	    				            intent.setClass(VideoBuyMembershipActivity.this, MainActivity.class);  
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
    			   	          		broadCastMessageToBoard("[����]"+starMyName(loginUserName)+"��"+new SimpleDateFormat("yyyy-MM-dd HH:mm:SS").format(new Date())+"�����ˡ�"+membershipdays+"�ջ�Ա����");
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

        },String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/user/json/addNewFilmAuth?uname="+loginUserName+"&filmID=999999&ordercode="+aliOrderCode.getText().toString().trim()+"&resType="+membershipdays+"", 200);  
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

					Toast.makeText( VideoBuyMembershipActivity.this, "���ڴ���...", Toast.LENGTH_LONG).show();
				} catch (FileNotFoundException e) {
					Toast.makeText( VideoBuyMembershipActivity.this, "ͼƬû�ҵ�,������ѡ��", Toast.LENGTH_LONG).show();
					processingAliTransLoadingHint.setVisibility(View.GONE);
					isProcessScreenshot = false;
				} catch (IOException e) {
					Toast.makeText( VideoBuyMembershipActivity.this, "ͼƬ��ȡ����,������ѡ��", Toast.LENGTH_LONG).show();
					processingAliTransLoadingHint.setVisibility(View.GONE);
					isProcessScreenshot = false;
				} catch (Exception e) {
					Toast.makeText( VideoBuyMembershipActivity.this, "ͼƬ�ϴ�ʧ��,�������ύ��"+e.getMessage(), Toast.LENGTH_LONG).show();
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
    };
    
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
		File imgFile = new File(realPath);
		
		params.put("userName", loginUserName);
		files.put("imageFile", imgFile);
		
		myScreenshotFolderPath = imgFile.getParentFile().getAbsolutePath();
		
		new MultipartFormHttpsPostThread(new Handler(){
			
			private boolean validCode(String message){
				String msg = message.replace("\n\n", "").replace(" ", "").replace("O", "0").replace("_", "").replace("[)", "");
				String[] lines = msg.split("\n");
				boolean foundAccoutTo = false;
				boolean foundOrderCode = false;
				String foundOrderCodeStr = "";
				
				for(String line:lines){
					
					if(membershipdays==1){
						line = line.toLowerCase();
						if(line.contains("prep**@yahoo")||line.contains("prep**@yaboo")){
							foundAccoutTo = true;
						}
			        }else if(membershipdays==31){
			        	line = line.toLowerCase();
			        	if(line.contains("prew**@yahoo")||line.contains("prew**@yaboo")){
							foundAccoutTo = true;
						}
			        }else if(membershipdays==93){
			        	line = line.toLowerCase();
			        	if(line.contains("prex**@yahoo")||line.contains("prex**@yaboo")){
							foundAccoutTo = true;
						}
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
					Toast.makeText( VideoBuyMembershipActivity.this, "��������������������Ժ����ԡ�", Toast.LENGTH_LONG).show();
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
						Toast.makeText( VideoBuyMembershipActivity.this, "���Ķ������벻��"+hoursDiff+","+dateString, Toast.LENGTH_LONG).show();
					}
					
				} catch (ParseException e) {
					CrashHandler.logErrorToFile(e);
				}
				
				
			}else{
				Toast.makeText( VideoBuyMembershipActivity.this, "��������ȷ�Ķ�������", Toast.LENGTH_LONG).show();
			}
    }
    

    

    private boolean checkIfProcessingAlipayTransaction() {
//    	CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(),
//				"alipay_trans_membership.dat", transID+"notakid"+membershipdays+"notakid"+loginUserName);
		
    	String content = CacheFileUtil.readCacheFile(getCacheDir().getAbsolutePath(), "alipay_trans_membership.dat");
    	if(content!=null && content.length()>0 && content.contains("notakid")){
    		final String[] alipayObjs = content.split("notakid");
    		if(alipayObjs.length>=2 && loginUserName!=null && loginUserName.equals(alipayObjs[2])){
	            
    			VideoBuyMembershipActivity.existingAliTransaction = alipayObjs[0]; 
                VideoBuyMembershipActivity.membershipdays=Integer.parseInt(alipayObjs[1]);
                
	            MakeSureDialog dialog = new MakeSureDialog();  
	          	 
	          	 dialog.setTextTitle("���������:�ϴ�֧����ͼ?");
	          	 dialog.setTextSureButton("�ϴ���ͼ");
	          	 dialog.setTextCancelButton("��ν�ͼ");
	          	 dialog.setContent("����ʱ��:"+alipayObjs[0]+"\n����"+membershipdays+"�ջ�Ա\n\n*��ȡ��ͼ:֧����>>�ҵ�>>�˵�>>�����굥>>����,Ȼ����'�ϴ���ͼ'");
	          	 
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
    	membershipdays = 0;
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
			
			List<File> screenshots = Utils.listTop1ScreenshotsIfAvailable(VideoBuyMembershipActivity.this,
					new File(myScreenshotFolderPath));
			if(screenshots!=null && screenshots.size()>0){
				final File screenshotRecommended = screenshots.get(screenshots.size()-1);
				
				final Dialog dia = new Dialog(VideoBuyMembershipActivity.this);
				
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
    
}
