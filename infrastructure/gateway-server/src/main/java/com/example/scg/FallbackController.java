package com.example.scg;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/ms1")
    public Mono<String> ms1Fallback() {
        return Mono.just("서비스1(MS1)이 현재 응답하지 않습니다. 잠시 후 다시 시도해주세요.");
    }

    @GetMapping("/ms2")
    public Mono<String> ms2Fallback() {
        return Mono.just("서비스2(MS2)가 현재 응답하지 않습니다. 잠시 후 다시 시도해주세요.");
    }

    @GetMapping("/ms3")
    public Mono<String> ms3Fallback() {
        return Mono.just("서비스3(MS3)이 현재 응답하지 않습니다. 잠시 후 다시 시도해주세요.");
    }

    @GetMapping("/ms4")
    public Mono<String> ms4Fallback() {
        return Mono.just("서비스4(MS4)가 현재 응답하지 않습니다. 잠시 후 다시 시도해주세요.");
    }

    @GetMapping("/ms5")
    public Mono<String> ms5Fallback() {
        return Mono.just("서비스5(MS5)가 현재 응답하지 않습니다. 잠시 후 다시 시도해주세요.");
    }
}