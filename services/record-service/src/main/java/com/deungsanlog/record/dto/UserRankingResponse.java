package com.deungsanlog.record.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRankingResponse {
    private int rank;
    private Long userId;
    private String nickname;
    private int recordCount;
}
