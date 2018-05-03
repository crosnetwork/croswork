package com.cros.block.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.cros.block.util.Page;

@Controller
@RequestMapping("/person")
public class PersonalController {

	
	
	/**
	 * 获取个人信息
	 * @return
	 */
	@RequestMapping("info")
	public Object personInfo(String id,Page page,int currPage){
		int currPageNo = 1;
		currPageNo = currPage;
		
		return null;
	}
	
}
