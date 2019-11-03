package com.ex.fivemao.io;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class WithEnc {
	
	public static void main(String[] args) throws Exception {
		File source = new File("C:/Users/premi/workspace-android/FiveMao/res/drawable-hdpi/pay_ali_feedback.PNG");
		File out = new File("C:/Users/premi/workspace-android/FiveMao/res/drawable-hdpi/post_pay_ali_feedback.PNG");
		
		withEncFile(source, out);
		
	}

	private static void withEncFile(File source, File out)
			throws FileNotFoundException, IOException {
		System.out.println("in:"+source.length());
		
		FileInputStream input = new FileInputStream(source);
		FileOutputStream output = new FileOutputStream(out);
		byte[] buffer = new byte[1024];
		int len;
		while((len=input.read(buffer))>0){
			
			buffer = encodeBuffer(buffer,0,len);
			
			output.write(buffer,0,len);
			System.out.println("reading:"+len);
		}
		
		input.close();
		output.close();
		
		System.out.println("out:"+out.length());
		
	}
	public static Bitmap getImageBigMapWithEnc(String imgFile){
		byte[] data;
		try {
			data = withEncFileBytes(new File(imgFile));
			return BitmapFactory.decodeByteArray(data, 0, data.length);
		} catch (Exception e) {
			return null;
		}
		
	}
	
	public static Bitmap getImageBigMapWithEnc(InputStream sourceFile){
		byte[] data;
		try {
			data = withEncFileBytesStream(sourceFile);
			return BitmapFactory.decodeByteArray(data, 0, data.length);
		} catch (Exception e) {
			return null;
		}
		
	}
	
	public static Bitmap getImageBigMapNoEnc(InputStream sourceFile){
		byte[] data;
		try {
			data = noEncFileBytesStream(sourceFile);
			return BitmapFactory.decodeByteArray(data, 0, data.length);
		} catch (Exception e) {
			return null;
		}
		
	}
	
	public static byte[] withEncFileBytes(File source) throws Exception {
		FileInputStream input = new FileInputStream(source);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		byte[] buffer = new byte[1024];
		int len;
		while((len=input.read(buffer))>0){
			buffer = encodeBuffer(buffer,0,len);
			output.write(buffer,0,len);
		}
		byte[] result = output.toByteArray();
		
		input.close();
		output.close();
		
		return result;
	}
	public static byte[] withEncFileBytesStream(InputStream input) throws Exception {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		byte[] buffer = new byte[1024];
		int len;
		while((len=input.read(buffer))>0){
			buffer = encodeBuffer(buffer,0,len);
			output.write(buffer,0,len);
		}
		byte[] result = output.toByteArray();
		
		input.close();
		output.close();
		
		return result;
	}
	
	public static byte[] noEncFileBytesStream(InputStream input) throws Exception {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		byte[] buffer = new byte[1024];
		int len;
		while((len=input.read(buffer))>0){
			output.write(buffer,0,len);
		}
		byte[] result = output.toByteArray();
		
		input.close();
		output.close();
		
		return result;
	}

	public static byte[] encodeBuffer(byte[] buffer, int start, int len) {
		
		if(buffer!=null && buffer.length>=(start+len)){
			for(int i = 0; i < buffer.length; i++){
				byte bdata = buffer[i];
				buffer[i] = (byte) ~bdata;
			}
		}
		return buffer;
	}
}
