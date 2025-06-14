package com.deungsanlog.record.dto;

public record HotMountainResponse(
        int rank,
        Long mountainId,
        String mountainName,
        Long recordCount
) {
}
