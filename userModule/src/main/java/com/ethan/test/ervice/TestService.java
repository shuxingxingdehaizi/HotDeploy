package com.ethan.test.ervice;

import org.springframework.stereotype.Service;

@Service("testService")
public class TestService {
	public String getUser(){
		return "This is getUser v1 :" +this.getClass().getClassLoader();
	}
}
