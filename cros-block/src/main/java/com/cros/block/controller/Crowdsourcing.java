package com.cros.block.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/crowd")
public class Crowdsourcing {
	
	@RequestMapping(value = "/defineCrowd", method = RequestMethod.GET)
	public String defineCrowd(){
		return "crowd/defineCrowd";
	}
	
	@RequestMapping(value = "/defineCrowdNext", method = RequestMethod.GET)
	public String defineCrowdNext(){
		return "crowd/defineCrowdNext";
	}
	
	@RequestMapping(value = "/defineCrowdDescription", method = RequestMethod.GET)
	public String defineCrowdDescription(){
		return "crowd/defineCrowdDescription";
	}
}
