package com.deungsanlog.mountain.dto;

import com.deungsanlog.mountain.entity.Mountain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MountainSearchResponse {

    private Long id;
    private String name;
    private String location;
    private Integer elevation;
    private Double latitude;
    private Double longitude;
    private String thumbnailImgUrl;

    // 검색 관련 추가 정보
    private String searchType;  // "NAME", "LOCATION", "BOTH"
    private Integer matchCount;  // 매칭 정확도 (정렬용)
    private String highlightedName;  // 검색어 하이라이트된 이름
    private String highlightedLocation;  // 검색어 하이라이트된 위치

    // Mountain 엔티티에서 변환하는 정적 메서드
    public static MountainSearchResponse from(Mountain mountain) {
        return MountainSearchResponse.builder()
                .id(mountain.getId())
                .name(mountain.getName())
                .location(mountain.getLocation())
                .elevation(mountain.getElevation())
                .latitude(mountain.getLatitude())
                .longitude(mountain.getLongitude())
                .thumbnailImgUrl(mountain.getThumbnailImgUrl())
                .searchType("BASIC")
                .matchCount(1)
                .build();
    }

    // 검색 타입과 매칭 정보를 포함한 변환 메서드
    public static MountainSearchResponse from(Mountain mountain, String searchType, Integer matchCount) {
        return MountainSearchResponse.builder()
                .id(mountain.getId())
                .name(mountain.getName())
                .location(mountain.getLocation())
                .elevation(mountain.getElevation())
                .latitude(mountain.getLatitude())
                .longitude(mountain.getLongitude())
                .thumbnailImgUrl(mountain.getThumbnailImgUrl())
                .searchType(searchType)
                .matchCount(matchCount)
                .build();
    }

    // 하이라이트 적용 메서드
    public MountainSearchResponse withHighlight(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            String keywordLower = keyword.toLowerCase();

            // 이름에 하이라이트 적용
            if (this.name != null && this.name.toLowerCase().contains(keywordLower)) {
                this.highlightedName = highlightText(this.name, keyword);
            } else {
                this.highlightedName = this.name;
            }

            // 위치에 하이라이트 적용
            if (this.location != null && this.location.toLowerCase().contains(keywordLower)) {
                this.highlightedLocation = highlightText(this.location, keyword);
            } else {
                this.highlightedLocation = this.location;
            }
        } else {
            this.highlightedName = this.name;
            this.highlightedLocation = this.location;
        }

        return this;
    }

    // 텍스트 하이라이트 헬퍼 메서드
    private String highlightText(String text, String keyword) {
        if (text == null || keyword == null) return text;

        String keywordLower = keyword.toLowerCase();
        String textLower = text.toLowerCase();

        int index = textLower.indexOf(keywordLower);
        if (index >= 0) {
            String before = text.substring(0, index);
            String match = text.substring(index, index + keyword.length());
            String after = text.substring(index + keyword.length());
            return before + "<mark>" + match + "</mark>" + after;
        }

        return text;
    }
}