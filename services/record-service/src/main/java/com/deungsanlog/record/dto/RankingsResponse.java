package com.deungsanlog.record.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingsResponse {
    private List<UserRankingResponse> topRankers;
    private UserRankingResponse myRank;
}

