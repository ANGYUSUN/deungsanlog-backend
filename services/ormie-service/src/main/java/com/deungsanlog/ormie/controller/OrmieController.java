package com.deungsanlog.ormie.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrmieController {

    @GetMapping("/ormie")
    public String mainPage() {
        return "main";  // resources/templates/main.html
    }
}
