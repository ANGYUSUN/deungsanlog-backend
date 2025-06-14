package com.deungsanlog.record.service;

import com.deungsanlog.record.client.UserClient;
import com.deungsanlog.record.dto.RankingsResponse;
import com.deungsanlog.record.dto.UserRankingResponse;
import com.deungsanlog.record.repository.RecordHikingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RecordHikingRepository recordHikingRepository;
    private final UserClient userClient;

    public RankingsResponse getRankingInfo(Long userId) {
        // 1) 상위 10명 뽑기 (unchanged)
        List<Object[]> topResults = recordHikingRepository.findTopRankers();
        List<UserRankingResponse> topRankers = new ArrayList<>();
        int rankCursor = 1;
        int sameRankCount = 0;
        Integer prevCount = null;
        int lastRank = 0;

        for (Object[] row : topResults) {
            if (row.length < 2) continue;

            Long uid = Long.parseLong(row[0].toString());
            int cnt = Integer.parseInt(row[1].toString());
            String nick = userClient.getNickname(uid);

            if (prevCount != null && cnt == prevCount) {
                sameRankCount++;
            } else {
                rankCursor += sameRankCount;
                sameRankCount = 1;
            }
            prevCount = cnt;

            if (rankCursor > 10) break; // 10등 초과 시 중단

            topRankers.add(UserRankingResponse.builder()
                    .rank(rankCursor)
                    .userId(uid)
                    .nickname(nick)
                    .recordCount(cnt)
                    .build());

            lastRank = rankCursor;
        }

// 10등이 여러 명일 경우 모두 포함
        for (int i = topRankers.size(); i < topResults.size(); i++) {
            Object[] row = topResults.get(i);
            if (row.length < 2) continue;
            int cnt = Integer.parseInt(row[1].toString());
            if (cnt == prevCount && lastRank == 10) {
                Long uid = Long.parseLong(row[0].toString());
                String nick = userClient.getNickname(uid);
                topRankers.add(UserRankingResponse.builder()
                        .rank(lastRank)
                        .userId(uid)
                        .nickname(nick)
                        .recordCount(cnt)
                        .build());
            } else {
                break;
            }
        }
        // 2) 내 순위 시도 (윈도우 함수)
        UserRankingResponse myRank = null;
        if (userId != null) {
            Object[] myRow = recordHikingRepository.findMyRanking(userId);

            if (myRow != null && myRow.length == 3) {
                // 정상적으로 window 함수 결과가 있을 때
                int myRankNum = Integer.parseInt(myRow[0].toString());
                Long myId = Long.parseLong(myRow[1].toString());
                int cnt = Integer.parseInt(myRow[2].toString());
                String nick = userClient.getNickname(myId);
                myRank = UserRankingResponse.builder()
                        .rank(myRankNum)
                        .userId(myId)
                        .nickname(nick)
                        .recordCount(cnt)
                        .build();
            } else {
                // fallback: 내 기록 개수 구하고, 그보다 많은 유저 수 +1 을 내 순위로 계산
                int myCount = recordHikingRepository.countByUserId(userId);
                if (myCount > 0) {
                    int higherCount = recordHikingRepository.countUsersWithMoreRecords(myCount);
                    String nick = userClient.getNickname(userId);
                    myRank = UserRankingResponse.builder()
                            .rank(higherCount + 1)
                            .userId(userId)
                            .nickname(nick)
                            .recordCount(myCount)
                            .build();
                }
                // myCount==0 이면 myRank는 여전히 null → 기록 없는 유저
            }
        }
        return RankingsResponse.builder()
                .topRankers(topRankers)
                .myRank(myRank)
                .build();
    }
}