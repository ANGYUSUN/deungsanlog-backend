package com.deungsanlog.ormie.controller;

import com.deungsanlog.ormie.dto.ChatRequestDto;
import com.deungsanlog.ormie.dto.ChatResponseDto;
import com.deungsanlog.ormie.service.OrmieService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ormie")
@RequiredArgsConstructor
public class OrmieController {

    private final OrmieService ormieService;

    @GetMapping("/status")
    public Map<String, String> getStatus() {
        return Map.of("message", "ormie-service is up!");
    }

    @PostMapping("/chat")
    public ChatResponseDto chat(@RequestBody ChatRequestDto requestDto) {
        return ormieService.ask(requestDto);
    }
}
