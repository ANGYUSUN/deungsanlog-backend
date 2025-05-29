package com.deungsanlog.ormie.service;

import com.deungsanlog.ormie.dto.ChatRequestDto;
import com.deungsanlog.ormie.dto.ChatResponseDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrmieService {

    @Value("${chatgpt.api.key}")
    private String apiKey;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public ChatResponseDto ask(ChatRequestDto requestDto) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");

        // 사용자 프로필 기반 system 메시지 생성
        String profileMessage = String.format(
                "당신은 등산 챗봇 오르미입니다. 사용자 정보는 다음과 같습니다: 나이: %s, 지역: %s, 등산 경험: %s. 이 정보를 참고해서 질문에 답해주세요.",
                requestDto.getAge(), requestDto.getRegion(), requestDto.getLevel()
        );

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", profileMessage),
                Map.of("role", "user", "content", requestDto.getMessage())
        );
        requestBody.put("messages", messages);

        String response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        String answer = extractContent(response);
        return new ChatResponseDto(answer);
    }


    private String extractContent(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            return "[오류] 응답 파싱 실패: " + e.getMessage();
        }
    }
}
