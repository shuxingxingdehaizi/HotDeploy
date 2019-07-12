package com.ethan.hotProcess.util;

import javax.servlet.http.HttpServletRequest;

public class URLResolver {
	public static String getComponentName(HttpServletRequest request){
		return request.getRequestURI().split("/")[1];
	}
}
