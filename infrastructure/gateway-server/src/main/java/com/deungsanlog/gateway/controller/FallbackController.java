package com.deungsanlog.gateway.controller;



import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/users")
    public Mono<String> usersFallback() {
        return Mono.just("유저서비스가 현재 응답하지 않습니다. 잠시 후 다시 시도해주세요.");
    }

    @GetMapping("/mountains")
    public Mono<String> mountainsFallback() {
        return Mono.just("산 서비스가 현재 응답하지 않습니다. 잠시 후 다시 시도해주세요.");
    }

    @GetMapping("/records")
    public Mono<String> recordsFallback() {return Mono.just("기록 서비스가 현재 응답하지 않습니다. 잠시 후 다시 시도해주세요.");
    }

    @GetMapping("/community")
    public Mono<String> communityFallback() {
        return Mono.just("커뮤니티 서비스가 현재 응답하지 않습니다. 잠시 후 다시 시도해주세요.");
    }

    @GetMapping("/meeting")
    public Mono<String> meetingFallback() {
        return Mono.just("모임 서비스가 현재 응답하지 않습니다. 잠시 후 다시 시도해주세요.");
    }

    @GetMapping("/ormie")
    public Mono<String> ormieFallback() {
        return Mono.just("오르미 서비스가 현재 응답하지 않습니다. 잠시 후 다시 시도해주세요.");
    }
    @GetMapping("/notification")
    public Mono<String> notificationFallback() {
        return Mono.just("알림 서비스가 현재 응답하지 않습니다. 잠시 후 다시 시도해주세요.");
    }
}