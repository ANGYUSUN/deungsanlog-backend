package com.example.test1.batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiMountainDto {
    private String mntnid;               // 산 코드 → mountains.external_id
    private String mntnnm;               // 산명 → mountains.name
    private String mntninfopoflc;        // 소재지 → mountains.location
    private Integer mntninfohght;            // 산 고도 → mountains.elevation
    private String mntnattchimageseq;    // 대표 이미지 → mountains.thumbnail_img_url

    private String mntnsbttlinfo;        // 부제 → mountain_descriptions.summary
    private String mntninfodscrt;        // 개관 → mountain_descriptions.full_description
    private String crcmrsghtnginfodscrt; // 주변 관광 정보 → mountain_descriptions.nearby_tour_info
}
