package com.ethan.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RestController;

import com.ethan.test.ervice.TestService;

@RestController
public class UserController {
	
	@Autowired
	@Qualifier("testService")
	private TestService testService;

	public String getUser(){
		return testService.getUser();
	}
}
