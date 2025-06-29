// 응답을 담을 DTO

package com.deungsanlog.ormie.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public class ChatResponseDto {
    private String answer; // ChatGPT가 응답한 메시지
}