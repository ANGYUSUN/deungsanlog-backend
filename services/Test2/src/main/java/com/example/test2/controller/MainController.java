package com.example.test2.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {


    @GetMapping("ms2/second")
    public String mainP(){
       return "main";
    }
}
