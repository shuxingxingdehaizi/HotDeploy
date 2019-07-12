package com.ethan.hotProcess.dispatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class HotDispatcher extends DispatcherServlet{
	private Log logger = LogFactory.getLog(HotDispatcher.class);
	
	private ClassPathXmlApplicationContext context;
	
	public void start(){
		logger.info("start context...");
		
		context = null;
		
		System.gc();
		
		context = new ClassPathXmlApplicationContext("classpath*:/**/application*.xml");
		
		context.setClassLoader(this.getClass().getClassLoader());
		
		context.refresh();
		
		super.onRefresh(context);
	}
	
	
	public void shutdown(){
		logger.info("Context is shutdowning ...");
		context.stop();
		this.context = null;
		System.gc();
		logger.info("Context shutdown complete");
	}
	
	@Override
	public void doService(HttpServletRequest req,HttpServletResponse resp) throws Exception{
		super.doService(req, resp);
	}
}
