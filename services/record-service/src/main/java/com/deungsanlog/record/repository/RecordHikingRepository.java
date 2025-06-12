package com.deungsanlog.record.repository;

import com.deungsanlog.record.domain.RecordHiking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecordHikingRepository extends JpaRepository<RecordHiking, Long> {
    int countByUserId(Long userId);

    List<RecordHiking> findByUserId(Long userId);

    @Query(value = """
            SELECT user_id, COUNT(*) as cnt
            FROM record_hikings
            GROUP BY user_id
            ORDER BY cnt DESC
            LIMIT 10
            """, nativeQuery = true)
    List<Object[]> findTopRankers();

    @Query(value = """
            SELECT r.rank, r.user_id, r.record_count FROM (
                SELECT user_id, COUNT(*) as record_count,
                       RANK() OVER (ORDER BY COUNT(*) DESC) as `rank`
                FROM record_hikings
                GROUP BY user_id
            ) r
            WHERE r.user_id = :userId
            """, nativeQuery = true)
    Object[] findMyRanking(@Param("userId") Long userId);

    @Query(value = """
            SELECT COUNT(*) FROM (
              SELECT user_id, COUNT(*) AS cnt
              FROM record_hikings
              GROUP BY user_id
              HAVING cnt > :count
            ) AS higher
            """, nativeQuery = true)
    int countUsersWithMoreRecords(@Param("count") int count);

}
