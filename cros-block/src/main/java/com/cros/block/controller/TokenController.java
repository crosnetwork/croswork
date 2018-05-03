package com.cros.block.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/token")
public class TokenController {

	@RequestMapping("/tokenInfo")
	public String toTokenPage(){
		return "token";
	}
	
}
