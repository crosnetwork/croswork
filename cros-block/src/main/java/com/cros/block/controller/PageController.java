package com.cros.block.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/page")
public class PageController {

	@RequestMapping("/index")
	public String toIndex(){
		return "index";
	}
	
	@RequestMapping("/publicKey")
	public String toDictPage(){
		return "publicKey";
	}
	
	@RequestMapping("/user")
	public String toPage(){
		return "userCenter";
	}
	
	@RequestMapping("/userManager")
	public String toUserPage(){
		return "userManager";
	}
	
	@RequestMapping("/mainChain")
	public String toMainChain(){
		return "mainChain";
	}
	
	@RequestMapping("/intContract")
	public String toIntContract(){
		return "intContract";
	}
	
	@RequestMapping("/defineContract")
	public String toDefineContract(){
		return "defineContract";
	}

	@RequestMapping("/sideChain")
	public String toSideChain(){
		return "sideChainBlock";
	}
	
	@RequestMapping("/tokenInfo")
	public String toTokenPage(){
		return "token";
	}
	
	@RequestMapping("/asset")
	public String toAssetPage(){
		return "asset";
	}
	@RequestMapping("/intContractList")
	public String toIntContractList(){
		return "intContractList";
	}
	@RequestMapping("/indexPage")
	public String toIndexPage(){
		return "indexPage";
	}
	@RequestMapping("record")
	public String toRecord(){
		return "blockRecord";
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
