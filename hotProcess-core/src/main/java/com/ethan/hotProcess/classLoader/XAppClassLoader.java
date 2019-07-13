package com.ethan.hotProcess.classLoader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class XAppClassLoader extends URLClassLoader{
	
	private String rootClassPath;

	public XAppClassLoader(URL[] urls,ClassLoader parent) {
		super(urls,parent);
	}
	
	public XAppClassLoader(String path,ClassLoader parent) {
		super(getAllURL(path+File.separator+"common"),parent);
		this.rootClassPath = path;
	}
	
	protected static URL[] getAllURL(String path){
		List<File>files = getAllClassFileUnderPath(path, new ArrayList<File>());
		if(files.isEmpty()){
//			throw new RuntimeException("No class file found!");
			return new URL[0];
		}
		URL[] urls = new URL[files.size()];
		int i = 0;
		for(File f : files){
			try {
				urls[i++] = f.toURI().toURL();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return urls;
	}
	
	public static List<File>getAllClassFileUnderPath(String path,List<File>files){
		File f = new File(path);
		if(!f.isDirectory()){
			if(f.getName().endsWith(".jar") || f.getName().endsWith(".class")){
				files.add(f);
			}
		}
		for(File cf : f.listFiles()){
			if(!f.isDirectory()){
				if(f.getName().endsWith(".jar") || f.getName().endsWith(".class")){
					files.add(f);
				}
			}else{
				getAllClassFileUnderPath(cf.getAbsolutePath(),files);
			}
		}
		return files;
	}
	
	@Override
	public URL[] getURLs(){
		return getAllURL(this.rootClassPath);
	}
	
	public String toString(){
		return super.toString()+",rootClassPath:"+this.rootClassPath;
	}

	public String getRootClassPath() {
		return rootClassPath;
	}

	public void setRootClassPath(String rootClassPath) {
		this.rootClassPath = rootClassPath;
	}
}
