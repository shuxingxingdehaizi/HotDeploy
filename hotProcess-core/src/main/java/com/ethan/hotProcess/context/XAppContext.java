package com.ethan.hotProcess.context;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.Lifecycle;

import com.ethan.hotProcess.classLoader.ClassLoaderFactory;
import com.ethan.hotProcess.classLoader.XAppClassLoader;
import com.ethan.hotProcess.util.PropertiesUtil;
import com.ethan.hotProcess.util.URLResolver;

public class XAppContext implements Lifecycle{
	
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * xApp名
	 */
	private String xAppName;
	
	/**
	 * 是否启动
	 */
	private AtomicBoolean running = new AtomicBoolean(false);
	
	/**
	 * xApp根目录
	 */
	private String appRootPath;
	
	/**
	 * App类加载器
	 */
	private XAppClassLoader appClassLoader;
	
	private List<ComponentContext>componentContexts = new ArrayList<ComponentContext>();
	
	private static String APP_COMMON_FOLDER = "common";
	
	private static String APP_COMPONENT_FOLDER = "component";
	
	private static String DEFAULT_LOAD_COMPONENT_VERSION = "v1";
	
	/**
	 * 启动时间
	 */
	private AtomicLong startTime = new AtomicLong();
	
	public XAppContext(){
		this.appRootPath = PropertiesUtil.getValue("appRootPath");
		this.xAppName = PropertiesUtil.getValue("xAppName");
	}
	
	public XAppContext init(){
		appClassLoader = ClassLoaderFactory.getAppClassLoader(appRootPath);
		
		File f = new File(appClassLoader.getRootClassPath()+File.separator+APP_COMPONENT_FOLDER);
		
		for(File component : f.listFiles()){
			if(!component.isDirectory()){
				continue;
			}
			String componentName = component.getName();
			for(File versionDir : component.listFiles()){
				if(DEFAULT_LOAD_COMPONENT_VERSION.equals(versionDir.getName())){
					componentContexts.add(new ComponentContext(this, componentName, DEFAULT_LOAD_COMPONENT_VERSION).init());
				}
			}
		}
		return this;
	}

	public String getxAppName() {
		return xAppName;
	}

	public String getAppRootPath() {
		return appRootPath;
	}

	public XAppClassLoader getAppClassLoader() {
		return appClassLoader;
	}

	public List<ComponentContext> getComponentContexts() {
		return componentContexts;
	}

	public AtomicLong getStartTime() {
		return startTime;
	}

	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return this.running.get();
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		long xstart = System.currentTimeMillis();
		logger.info("=========================Begin to start xApp["+xAppName+"]=========================");
		
		if(running.get()){
			logger.info("xApp is running!");
			return;
		}
		
		for(ComponentContext componentContext : componentContexts){
			try {
				long start = System.currentTimeMillis();
				logger.info("================Begin start component["+componentContext.getComponentName()+"]...================");
				componentContext.start();
				logger.info("================Component["+componentContext.getComponentName()+"] start complete in "+(System.currentTimeMillis()-start)+"ms================");
			} catch (Exception e) {
				logger.error("================Component["+componentContext.getComponentName()+"] start fail================",e);
			}
		}
		running.set(true);
		
		startTime.set(new Date().getTime());
		
		logger.info("=========================xApp["+xAppName+"] start complete in "+(System.currentTimeMillis()-xstart)+"ms=========================");
	}
	
	public void start(String component,String version) {
		if(!running.get()){
			logger.info("xApp is not running!");
			throw new RuntimeException("xApp is not running!");
		}
		ComponentContext componentContext = getComponentContext(component);
		
		if(componentContext == null){
			throw new RuntimeException("Component["+component+"] not found!");
		}
		
		if(componentContext.isRunning()){
			throw new RuntimeException("Component["+component+"] is running,stop it first!");
		}
		
		long start = System.currentTimeMillis();
		
		logger.info("================Begin start component["+componentContext.getComponentName()+"]...================");
		
		this.removeComponentContext(component);
		
		componentContext = new ComponentContext(this, component, version);
		
		componentContext.start();
		
		this.componentContexts.add(componentContext);
		
		startTime.set(new Date().getTime());
		logger.info("================Component["+componentContext.getComponentName()+"] start complete in "+(System.currentTimeMillis()-start)+"ms================");
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		long xstart = System.currentTimeMillis();
		logger.info("=========================Begin to stop xApp["+xAppName+"]=========================");
		
		for(ComponentContext componentContext : componentContexts){
			long start = System.currentTimeMillis();
			logger.info("================Begin stop component["+componentContext.getComponentName()+"]...================");
			
			componentContext.stop();
			
			this.removeComponentContext(componentContext.getComponentName());
			
			logger.info("================Component["+componentContext.getComponentName()+"] stop complete in "+(System.currentTimeMillis()-start)+"ms================");
		}
		
		running.set(false);
		
		System.gc();
		
		logger.info("=========================xApp["+xAppName+"] stop complete in "+(System.currentTimeMillis()-xstart)+"ms=========================");
	}
	
	public void stop(String component){
		if(!running.get()){
			logger.info("xApp is not running!");
			throw new RuntimeException("xApp is not running!");
		}
		ComponentContext componentContext = getComponentContext(component);
		
		if(componentContext == null){
			throw new RuntimeException("Component["+component+"] not found!");
		}
		
		if(!componentContext.isRunning()){
			throw new RuntimeException("Component["+component+"] is not running!");
		}
		
		long start = System.currentTimeMillis();
		logger.info("================Begin stop component["+componentContext.getComponentName()+"]...================");
		
		componentContext.stop();
		
		System.gc();
		
		logger.info("================Component["+componentContext.getComponentName()+"] stop complete in "+(System.currentTimeMillis()-start)+"ms================");
	}
	
	
	public void dispatch(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException{
		ComponentContext context = locatComponentContext(request);
		
		if(context == null){
			logger.error("Component for url["+request.getRequestURL()+"] not found!");
			response.setStatus(404);
			return;
		}
		
		if(!context.isRunning()){
			logger.info("component["+context.getComponentName()+"] is not running!");
		}
		context.dispatch(request, response);
	}
	
	private ComponentContext locatComponentContext(HttpServletRequest request){
		String component = URLResolver.getComponentName(request);
		for(ComponentContext componentContext : componentContexts){
			if(component.equals(componentContext.getComponentName())){
				return componentContext;
			}
		}
		return null;
	}
	
	private ComponentContext getComponentContext(String componentName){
		for(ComponentContext componentContext : componentContexts){
			if(componentName.equals(componentContext.getComponentName())){
				return componentContext;
			}
		}
		return null;
	}
	
	private ComponentContext removeComponentContext(String component){
		ComponentContext context = null;
		Iterator<ComponentContext>ite = componentContexts.iterator();
		while(ite.hasNext()){
			context = ite.next();
			if(component.equals(context.getComponentName())){
				ite.remove();
			}
		}
		return context;
	}
}
