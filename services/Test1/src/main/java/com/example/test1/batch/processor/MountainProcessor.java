package com.example.test1.batch.processor;

import com.example.test1.batch.client.MountainApiClient;
import com.example.test1.batch.dto.ApiMountainDto;
import com.example.test1.batch.dto.MountainMeta;
import com.example.test1.domain.Mountain;
import com.example.test1.domain.MountainDescription;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MountainProcessor implements ItemProcessor<MountainMeta, Mountain> {

    private final MountainApiClient apiClient;

    @Override
    public Mountain process(MountainMeta meta) {
        // 1. API에서 산 이름으로 조회
        List<ApiMountainDto> results = apiClient.searchByName(meta.getName());

        // 2. 이름이 정확히 같은 항목 중 고도 차이 최소 선택
        ApiMountainDto matched = results.stream()
                .filter(dto -> dto.getMntninfohght() != null)
                .filter(dto -> meta.getName().equals(dto.getMntnnm())) // 이름 완전 일치
                .min(Comparator.comparingDouble(dto -> Math.abs(dto.getMntninfohght() - meta.getHeight())))
                .orElse(null);

        if (matched == null) {
            System.out.println("❌ No exact match found for: " + meta.getName() + ", height: " + meta.getHeight());
            return null;
        } else {
            System.out.println("✅ Matched: " + matched.getMntnnm() + ", API 고도: " + matched.getMntninfohght() + ", 메타 고도: " + meta.getHeight());
        }

        // 3. Entity 생성
        Mountain mountain = Mountain.builder()
                .name(matched.getMntnnm())
                .externalId(matched.getMntnid())
                .location(matched.getMntninfopoflc())
                .elevation(matched.getMntninfohght())
                .thumbnailImgUrl(matched.getMntnattchimageseq())
                .build();

        MountainDescription description = MountainDescription.builder()
                .mountain(mountain)
                .summary(matched.getMntnsbttlinfo())
                .fullDescription(matched.getMntninfodscrt())
                .nearbyTourInfo(matched.getCrcmrsghtnginfodscrt())
                .build();

        mountain.setDescription(description);

        return mountain;
    }
}
