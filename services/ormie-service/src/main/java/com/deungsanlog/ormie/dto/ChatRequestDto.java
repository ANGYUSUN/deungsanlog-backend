// 질문을 담을 DTO

package com.deungsanlog.ormie.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class ChatRequestDto {
    private String message;
}