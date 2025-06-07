// 질문을 담을 DTO

package com.deungsanlog.ormie.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class ChatRequestDto {
    private String message;

    private String age;
    private String region;
    private String level;
}