package com.example.test1.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {


    @GetMapping("ms1/first")
    public String mainP(){
       return "main";
    }
}
