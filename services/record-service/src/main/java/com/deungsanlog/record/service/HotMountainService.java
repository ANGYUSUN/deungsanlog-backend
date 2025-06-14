package com.deungsanlog.record.service;

import com.deungsanlog.record.dto.HotMountainResponse;
import com.deungsanlog.record.repository.RecordHikingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HotMountainService {

    private final RecordHikingRepository recordHikingRepository;

    public List<HotMountainResponse> getHotMountains(int limit) {
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        List<Object[]> result = recordHikingRepository.findRankedHotMountains(oneMonthAgo, limit);

        return result.stream()
                .map(r -> new HotMountainResponse(
                        ((Number) r[0]).intValue(),      // rank
                        ((Number) r[1]).longValue(),     // mountainId
                        (String) r[2],                   // mountainName
                        ((Number) r[3]).longValue()      // recordCount
                ))
                .toList();
    }
}