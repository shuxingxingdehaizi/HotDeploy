package com.ethan.test.ervice;

import org.springframework.stereotype.Service;

@Service("testService")
public class TestService {
	public String getProd(){
		return "This is prodModule.getProd v2��"+this.getClass().getClassLoader();
	}
}
