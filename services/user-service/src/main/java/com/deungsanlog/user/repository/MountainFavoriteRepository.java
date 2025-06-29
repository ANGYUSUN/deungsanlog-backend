package com.deungsanlog.user.repository;

import com.deungsanlog.user.entity.MountainFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MountainFavoriteRepository extends JpaRepository<MountainFavorite, Long> {

    /**
     * 사용자의 모든 즐겨찾기 목록 조회
     *
     * @param userId 사용자 ID
     * @return 즐겨찾기 목록 (생성일 최신순)
     */
    List<MountainFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 특정 사용자가 특정 산을 즐겨찾기했는지 확인
     *
     * @param userId     사용자 ID
     * @param mountainId 산 ID
     * @return 즐겨찾기 여부
     */
    boolean existsByUserIdAndMountainId(Long userId, Long mountainId);

    /**
     * 특정 사용자의 특정 산 즐겨찾기 조회
     *
     * @param userId     사용자 ID
     * @param mountainId 산 ID
     * @return 즐겨찾기 (Optional)
     */
    Optional<MountainFavorite> findByUserIdAndMountainId(Long userId, Long mountainId);

    /**
     * 사용자의 즐겨찾기 개수 조회
     *
     * @param userId 사용자 ID
     * @return 즐겨찾기 개수
     */
    int countByUserId(Long userId);

    /**
     * 특정 산을 즐겨찾기한 사용자 수
     *
     * @param mountainId 산 ID
     * @return 즐겨찾기한 사용자 수
     */
    int countByMountainId(Long mountainId);

    /**
     * 사용자의 즐겨찾기 산 ID 목록만 조회 (성능 최적화)
     *
     * @param userId 사용자 ID
     * @return 산 ID 목록
     */
    @Query("SELECT mf.mountainId FROM MountainFavorite mf WHERE mf.userId = :userId ORDER BY mf.createdAt DESC")
    List<Long> findMountainIdsByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * 특정 산을 즐겨찾기한 사용자 ID 목록 조회 (알림 전송용)
     *
     * @param mountainId 산 ID
     * @return 사용자 ID 목록
     */
    @Query("SELECT mf.userId FROM MountainFavorite mf WHERE mf.mountainId = :mountainId")
    List<Long> findUserIdsByMountainId(@Param("mountainId") Long mountainId);

    /**
     * 사용자의 즐겨찾기를 모두 삭제 (회원 탈퇴 시 사용)
     *
     * @param userId 사용자 ID
     */
    void deleteByUserId(Long userId);

    /**
     * 특정 산의 모든 즐겨찾기를 삭제 (산 정보 삭제 시 사용)
     *
     * @param mountainId 산 ID
     */
    void deleteByMountainId(Long mountainId);

    /**
     * 특정 사용자의 특정 산 즐겨찾기 삭제
     *
     * @param userId     사용자 ID
     * @param mountainId 산 ID
     * @return 삭제된 행 수
     */
    int deleteByUserIdAndMountainId(Long userId, Long mountainId);
}