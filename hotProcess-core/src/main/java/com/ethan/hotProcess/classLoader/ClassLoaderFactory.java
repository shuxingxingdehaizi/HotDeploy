package com.ethan.hotProcess.classLoader;

public class ClassLoaderFactory {
	public static XAppClassLoader getAppClassLoader(String appRootPath){
		return new XAppClassLoader(appRootPath, ClassLoaderFactory.class.getClassLoader());
	}
	
	public static ComponentClassLoader getComponentClassLoadeer(XAppClassLoader appClassLoader,String componentName,String version){
		return new ComponentClassLoader(componentName, version, appClassLoader);
	}
}
