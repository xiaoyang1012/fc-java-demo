package com.aliyun.fc.example.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/demo")
public class HelloController {
    
    @RequestMapping("/hello")
    public String index(){
        return "index";
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/test", produces="application/json;charset=UTF-8")
    public ResponseEntity<String> hello() {
        return new ResponseEntity<String>("Hello alibaba 阿里巴巴", HttpStatus.OK);
    }
}
