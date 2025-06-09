package com.deungsanlog.record.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BadgeProfileDto {
    private int stage;
    private String title;
    private String description;
    private String nickname;
}
