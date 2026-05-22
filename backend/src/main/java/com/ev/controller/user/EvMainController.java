package com.ev.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class EvMainController {
	
	@GetMapping("/main")
	public String main() {
			return "user/main/main";
	}
}
