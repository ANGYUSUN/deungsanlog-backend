package com.deungsanlog.meeting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MeetingController {

    @GetMapping("/meetings")
    public String mainPage() {
        return "main";
    }

}