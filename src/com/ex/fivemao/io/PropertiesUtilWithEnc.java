package com.ex.fivemao.io;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;


public class PropertiesUtilWithEnc {
	public static Properties prop = new Properties();
	
	static{
		try {
			String file = "post_wumao.properties";
			byte[] data = WithEnc.withEncFileBytesStream(PropertiesUtilWithEnc.class.getClassLoader().getResourceAsStream(file));
			prop.load(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data))));
		} catch (Exception e) {
			prop.put("_propertyLoadError", e.getMessage());
		}
	}
	
	public static void main(String[] args) throws Exception {
	}
	
	public static Properties readProperties(String classPathPropFile){
		try {
			prop.load(new BufferedReader(new InputStreamReader(PropertiesUtilWithEnc.class.getClassLoader().getResourceAsStream(classPathPropFile))));
			return prop;
		} catch (IOException e) {
			prop.put("_propertyLoadError", e.getMessage());
			return prop;
		}
	}
	
	public static String getString(String key){
		if(prop!=null){
			return String.valueOf(prop.get(key));
		}
		return null;
	}
	
}
