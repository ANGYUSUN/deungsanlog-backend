package com.deungsanlog.record.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RecordController {

    @GetMapping("/records")
    public String mainPage() {
        return "main";  // resources/templates/main.html
    }
}
