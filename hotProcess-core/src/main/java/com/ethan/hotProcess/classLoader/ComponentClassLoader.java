package com.ethan.hotProcess.classLoader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import com.ethan.hotProcess.util.PropertiesUtil;

public class ComponentClassLoader extends URLClassLoader{
	
	private String componentRootPath;
	
	private String appRootPath;
	
	private String version;
	
	private String componentName;

	public ComponentClassLoader(URL[] urls,ClassLoader parent) {
		super(urls,parent);
	}
	
	public ComponentClassLoader(String componentName,String version,XAppClassLoader parent) {
		super(getAllURL(parent.getRootClassPath(),componentName,version),parent);
		this.componentRootPath = parent.getRootClassPath()+File.separator+"component"+File.separator+componentName+File.separator+version;
		this.appRootPath = parent.getRootClassPath();
		this.componentName = componentName;
		this.version = version;
	}
	
	protected static URL[] getAllURL(String appRootPath,String componentName,String version){
		List<File>files = getAllClassFileUnderPath(appRootPath+File.separator+"component"+File.separator+componentName+File.separator+version, new ArrayList<File>());
		if(files.isEmpty()){
			throw new RuntimeException("No class file found!");
		}
		URL[] urls = new URL[files.size()+1];
		int i = 0;
		try {
			for(File f : files){
				urls[i++] = f.toURI().toURL();
				urls[files.size()] = new File(appRootPath+File.separator+"component"+File.separator+PropertiesUtil.getValue("dispatcherClassFile")).toURI().toURL();
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException("Error occurs when getAllURL",e);
		}
		
		
		return urls;
	}
	
	public static List<File>getAllClassFileUnderPath(String path,List<File>files){
		File f = new File(path);
		if(!f.isDirectory()){
			if(f.getName().endsWith(".jar") || f.getName().endsWith(".class")){
				files.add(f);
			}
		}else{
			for(File cf : f.listFiles()){
				if(!f.isDirectory()){
					if(f.getName().endsWith(".jar") || f.getName().endsWith(".class")){
						files.add(f);
					}
				}else{
					getAllClassFileUnderPath(cf.getAbsolutePath(),files);
				}
			}
		}
		return files;
	}
	
	@Override
	public URL[] getURLs(){
		return getAllURL(this.appRootPath,componentName,version);
	}
	
	public String toString(){
		return super.toString()+",rootClassPath:"+this.appRootPath;
	}

	public String getAppRootPath() {
		return appRootPath;
	}
}
