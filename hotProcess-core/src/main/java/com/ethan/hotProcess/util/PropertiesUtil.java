package com.ethan.hotProcess.util;

import java.util.Properties;

public class PropertiesUtil {
	public static Properties p;
	
	static{
		p = new Properties();
		try {
			p.load(PropertiesUtil.class.getResourceAsStream("/application.properties"));
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public static String getValue(String k){
		return p.getProperty(k);
	}
}
