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
        String name = meta.getName();
        Double metaHeight = meta.getHeight();

        // 1. 첫 글자를 기반으로 API 검색
        String keyword = name.substring(0, 1);
        List<ApiMountainDto> results = apiClient.searchByKeyword(keyword);
        System.out.println("🔍 API 응답 수 (" + keyword + "): " + results.size());

        // 2. API 결과에서 이름이 완전 일치하거나, name으로 시작하는 항목만 필터링
        List<ApiMountainDto> sameNameList = results.stream()
                .filter(dto -> dto.getMntnnm() != null && dto.getMntnnm().startsWith(name))
                .filter(dto -> dto.getMntninfohght() != null)
                .toList();

        if (sameNameList.isEmpty()) {
            System.out.println("❌ No suitable match for: " + name);
            return null;
        }

        // 3. 고도 차이 최소 항목 선택
        ApiMountainDto matched = sameNameList.stream()
                .min(Comparator.comparingDouble(dto -> Math.abs(dto.getMntninfohght() - metaHeight)))
                .orElse(null);

        if (matched == null) {
            System.out.println("❌ No elevation match found for: " + name);
            return null;
        }

        System.out.println("✅ 매칭 완료: " + matched.getMntnnm()
                + " | API 고도: " + matched.getMntninfohght()
                + " | 메타 고도: " + metaHeight);

        // 4. Entity 생성
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
