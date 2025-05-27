package com.deungsanlog.mountain.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {

    @GetMapping("/second")
    public String mainP() {
        return "main";
    }

    @GetMapping("/api/test")
    @ResponseBody
    public String apiTest() {
        return "Mountain Service OK!";
    }
}