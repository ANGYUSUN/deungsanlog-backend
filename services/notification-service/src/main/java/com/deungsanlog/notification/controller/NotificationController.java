package com.deungsanlog.notification.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NotificationController {

    @GetMapping("/notification")
    public String mainPage() {
        return "main";  // resources/templates/main.html
    }
}
