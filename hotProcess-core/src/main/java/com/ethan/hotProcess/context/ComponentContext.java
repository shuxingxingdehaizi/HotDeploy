package com.ethan.hotProcess.context;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.Lifecycle;

import com.ethan.hotProcess.classLoader.ClassLoaderFactory;
import com.ethan.hotProcess.classLoader.ComponentClassLoader;
import com.ethan.hotProcess.constant.Constant;

public class ComponentContext implements Lifecycle{
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * 当前组件所属APP
	 */
	private XAppContext xApp;
	
	/**
	 * 组件版本
	 */
	private String version;
	
	/**
	 * 组件名称
	 */
	private String componentName;
	
	/**
	 * 组件类加载器
	 */
	private ComponentClassLoader classLoader;
	
	/**
	 * 是否启动
	 */
	private AtomicBoolean runing = new AtomicBoolean(false);
	
	/**
	 * 转发器
	 */
	private Object dispatcherInstance;
	
	/**
	 * 启动时间
	 */
	private AtomicLong startTime = new AtomicLong();
	
	public ComponentContext(XAppContext xApp,String componentName,String verson){
		super();
		this.xApp = xApp;
		this.version = verson;
		this.componentName = componentName;
	}
	
	public ComponentContext init(){
		this.classLoader = ClassLoaderFactory.getComponentClassLoadeer(this.xApp.getAppClassLoader(), componentName, version);
		return this;
	}

	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return runing.get();
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		try {
			if(runing.get()){
				throw new RuntimeException("Component["+componentName+"] is running,shut it down first!");
			}
			
			Class dispatcher = classLoader.loadClass("com.ethan.hotProcess.dispatcher.HotDispatcher");
			
			if(dispatcher == null){
				throw new ClassNotFoundException("Can not findd class : com.ethan.hotProcess.dispatcher.HotDispatcher");
			}
			
			Thread.currentThread().setContextClassLoader(classLoader);
			
			dispatcherInstance = dispatcher.newInstance();
			
			dispatcher.getMethod(Constant.ACTION_START).invoke(dispatcherInstance);
			
			runing.set(true);
			
			startTime.set(new Date().getTime());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			logger.error("Error occurs when start xApp",e);
		}
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		try {
			if(!runing.get()){
				throw new RuntimeException("Component["+componentName+"] is not running!");
			}
			
			Thread.currentThread().setContextClassLoader(classLoader);
			
			dispatcherInstance.getClass().getMethod(Constant.ACTION_SHUTDOWN).invoke(dispatcherInstance);
			
			runing.set(true);
			
			startTime.set(new Date().getTime());
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			logger.error("Error occurs when stop xApp",e);
		}
	}
	
	public void dispatch(HttpServletRequest request,HttpServletResponse response){
		try {
			dispatcherInstance.getClass().getDeclaredMethod("doService", 
					classLoader.loadClass("javax.servlet.http.HttpServletRequest"),
					classLoader.loadClass("javax.servlet.http.HttpServletResponse")
					).invoke(dispatcherInstance, request,response);
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			logger.error("Error occurs when xApp dispatch request",e);
		}
	}

	public XAppContext getxApp() {
		return xApp;
	}

	public String getVersion() {
		return version;
	}

	public String getComponentName() {
		return componentName;
	}

	public ComponentClassLoader getClassLoader() {
		return classLoader;
	}

	public AtomicLong getStartTime() {
		return startTime;
	}
}
