package com.ethan.hotProcess.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.DispatcherServlet;

import com.alibaba.fastjson.JSON;
import com.ethan.hotProcess.constant.Constant;
import com.ethan.hotProcess.context.ComponentContext;
import com.ethan.hotProcess.context.XAppContext;
import com.ethan.hotProcess.dto.AppStatusReportDto;

//@WebServlet(name="hotProcessServlet",urlPatterns="/*",asyncSupported=true)
public class HotProcessServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 15142456234L;
	
	private Log logger = LogFactory.getLog(this.getClass());
	
	private ServletContext servletContext;
	
	private ApplicationContext springApplicationContext;
	
	private DispatcherServlet dispatcherServlet;
	
	private XAppContext appContext;
	
	public void init() throws ServletException{
		super.init();
		servletContext = this.getServletContext();
		springApplicationContext = (ApplicationContext) WebApplicationContextUtils.getWebApplicationContext(servletContext);
		dispatcherServlet = springApplicationContext.getBean(DispatcherServlet.class);
		dispatcherServlet.init();
	}
	
	public HotProcessServlet(){
		intitAppContext();
	}
	
	public void intitAppContext(){
		appContext = new XAppContext();
		appContext.init();
		appContext.start();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try { 
			hotProcess(req, resp);
		} catch (Exception e) {
			logger.error("Error occurs when hot process",e);
			OutputStream os = resp.getOutputStream();
			os.write(e.getMessage().getBytes(Constant.DEFAULT_CHAR_SET));
			os.flush();
		}
	}
	
	private void hotProcess(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
		if(appContext == null || (!appContext.isRunning())){
			throw new RuntimeException("App is not running!");
		}
		if(req.getRequestURI().equals("manage")){
			manage(req, resp);
		}else{
			appContext.dispatch(req, resp);
		}
	}
	
	private void manage(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		String action = req.getParameter("action");
		
		String result = "";
		if(Constant.ACTION_START.equals(action)){
			String version = req.getParameter("v");
			String component = req.getParameter("component");
			appContext.start(component, version);
			result = "success";
		}else if(Constant.ACTION_SHUTDOWN.equals(action)){
			String component = req.getParameter("component");
			appContext.stop(component);
			result = "success";
		}else if(Constant.ACTION_REPORT.equals(action)){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			AppStatusReportDto report = new AppStatusReportDto();
			report.setxAppName(this.appContext.getxAppName());
			report.setStatus(appContext.isRunning() ? AppStatusReportDto.STATUS_RUNNING : AppStatusReportDto.STATUS_STOP);
			report.setStartTime(sdf.format(new Date(appContext.getStartTime().get())));
			for(ComponentContext componentContext : appContext.getComponentContexts()){
				report.addComponent(componentContext.getComponentName(), 
								sdf.format(new Date(componentContext.getStartTime().get())), 
								componentContext.isRunning() ? AppStatusReportDto.STATUS_RUNNING : AppStatusReportDto.STATUS_STOP, 
								componentContext.getVersion());
			}
			result = JSON.toJSONString(report);
		}
		
		OutputStream os = resp.getOutputStream();
		os.write(result.getBytes(Constant.DEFAULT_CHAR_SET));
		os.flush();
	}

}
