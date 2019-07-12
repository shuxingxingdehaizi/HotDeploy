package com.ethan.hotProcess.dto;

import java.util.ArrayList;
import java.util.List;

public class AppStatusReportDto {
	public static final String STATUS_RUNNING = "running";
	
	public static final String STATUS_STOP="stop";
	
	private String xAppName;
	
	private String startTime;
	
	private String status;
	
	private List<ComponentStatusDto>components = new ArrayList<ComponentStatusDto>();
	
	public static class ComponentStatusDto{
		private String componentName;
		
		private String startTime;
		
		private String status;
		
		private String version;

		public ComponentStatusDto(String componentName, String startTime, String status, String version) {
			super();
			this.componentName = componentName;
			this.startTime = startTime;
			this.status = status;
			this.version = version;
		}

		public String getComponentName() {
			return componentName;
		}

		public String getStartTime() {
			return startTime;
		}

		public String getStatus() {
			return status;
		}

		public String getVersion() {
			return version;
		}

		public void setComponentName(String componentName) {
			this.componentName = componentName;
		}

		public void setStartTime(String startTime) {
			this.startTime = startTime;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public void setVersion(String version) {
			this.version = version;
		}
	}

	public String getxAppName() {
		return xAppName;
	}

	public String getStartTime() {
		return startTime;
	}

	public String getStatus() {
		return status;
	}

	public List<ComponentStatusDto> getComponents() {
		return components;
	}

	public void setxAppName(String xAppName) {
		this.xAppName = xAppName;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setComponents(List<ComponentStatusDto> components) {
		this.components = components;
	}
	
	public void addComponent(String componentName,String startTime,String status,String version){
		this.components.add(new ComponentStatusDto(componentName, startTime, status, version));
	}
}
