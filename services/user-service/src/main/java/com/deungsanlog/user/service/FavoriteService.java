package com.deungsanlog.user.service;

import com.deungsanlog.user.entity.MountainFavorite;
import com.deungsanlog.user.repository.MountainFavoriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FavoriteService {

    private final MountainFavoriteRepository favoriteRepository;

    /**
     * 즐겨찾기 추가
     *
     * @param userId     사용자 ID
     * @param mountainId 산 ID
     * @return 추가 성공 여부
     */
    public boolean addFavorite(Long userId, Long mountainId) {
        log.info("즐겨찾기 추가 요청: userId={}, mountainId={}", userId, mountainId);

        try {
            // 이미 즐겨찾기에 있는지 확인
            if (favoriteRepository.existsByUserIdAndMountainId(userId, mountainId)) {
                log.warn("이미 즐겨찾기에 추가된 산: userId={}, mountainId={}", userId, mountainId);
                return false;
            }

            // 즐겨찾기 추가
            MountainFavorite favorite = MountainFavorite.builder()
                    .userId(userId)
                    .mountainId(mountainId)
                    .build();

            favoriteRepository.save(favorite);
            log.info("즐겨찾기 추가 성공: userId={}, mountainId={}", userId, mountainId);
            return true;

        } catch (DataIntegrityViolationException e) {
            log.error("즐겨찾기 추가 실패 - 중복 데이터: userId={}, mountainId={}", userId, mountainId, e);
            return false;
        } catch (Exception e) {
            log.error("즐겨찾기 추가 중 오류 발생: userId={}, mountainId={}", userId, mountainId, e);
            throw new RuntimeException("즐겨찾기 추가 실패", e);
        }
    }

    /**
     * 즐겨찾기 삭제
     *
     * @param userId     사용자 ID
     * @param mountainId 산 ID
     * @return 삭제 성공 여부
     */
    public boolean removeFavorite(Long userId, Long mountainId) {
        log.info("즐겨찾기 삭제 요청: userId={}, mountainId={}", userId, mountainId);

        try {
            int deletedCount = favoriteRepository.deleteByUserIdAndMountainId(userId, mountainId);

            if (deletedCount > 0) {
                log.info("즐겨찾기 삭제 성공: userId={}, mountainId={}", userId, mountainId);
                return true;
            } else {
                log.warn("삭제할 즐겨찾기가 없음: userId={}, mountainId={}", userId, mountainId);
                return false;
            }

        } catch (Exception e) {
            log.error("즐겨찾기 삭제 중 오류 발생: userId={}, mountainId={}", userId, mountainId, e);
            throw new RuntimeException("즐겨찾기 삭제 실패", e);
        }
    }

    /**
     * 즐겨찾기 여부 확인
     *
     * @param userId     사용자 ID
     * @param mountainId 산 ID
     * @return 즐겨찾기 여부
     */
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long mountainId) {
        log.debug("즐겨찾기 여부 확인: userId={}, mountainId={}", userId, mountainId);
        return favoriteRepository.existsByUserIdAndMountainId(userId, mountainId);
    }

    /**
     * 사용자의 즐겨찾기 산 ID 목록 조회
     *
     * @param userId 사용자 ID
     * @return 즐겨찾기 산 ID 목록 (최신순)
     */
    @Transactional(readOnly = true)
    public List<Long> getFavoriteIds(Long userId) {
        log.info("사용자 즐겨찾기 ID 목록 조회: userId={}", userId);
        return favoriteRepository.findMountainIdsByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 사용자의 즐겨찾기 개수 조회
     *
     * @param userId 사용자 ID
     * @return 즐겨찾기 개수
     */
    @Transactional(readOnly = true)
    public int getFavoriteCount(Long userId) {
        log.debug("사용자 즐겨찾기 개수 조회: userId={}", userId);
        return favoriteRepository.countByUserId(userId);
    }

    /**
     * 사용자의 즐겨찾기 전체 목록 조회 (관리용)
     *
     * @param userId 사용자 ID
     * @return 즐겨찾기 전체 정보 목록
     */
    @Transactional(readOnly = true)
    public List<MountainFavorite> getUserFavorites(Long userId) {
        log.info("사용자 즐겨찾기 전체 목록 조회: userId={}", userId);
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 즐겨찾기 토글 (있으면 삭제, 없으면 추가)
     *
     * @param userId     사용자 ID
     * @param mountainId 산 ID
     * @return 토글 후 상태 (true: 추가됨, false: 삭제됨)
     */
    public boolean toggleFavorite(Long userId, Long mountainId) {
        log.info("즐겨찾기 토글 요청: userId={}, mountainId={}", userId, mountainId);

        if (isFavorite(userId, mountainId)) {
            removeFavorite(userId, mountainId);
            log.info("즐겨찾기 토글 결과: 삭제됨 - userId={}, mountainId={}", userId, mountainId);
            return false;
        } else {
            addFavorite(userId, mountainId);
            log.info("즐겨찾기 토글 결과: 추가됨 - userId={}, mountainId={}", userId, mountainId);
            return true;
        }
    }

    /**
     * 특정 산을 즐겨찾기한 사용자 수 조회
     *
     * @param mountainId 산 ID
     * @return 즐겨찾기한 사용자 수
     */
    @Transactional(readOnly = true)
    public int getMountainFavoriteCount(Long mountainId) {
        log.debug("산 즐겨찾기 사용자 수 조회: mountainId={}", mountainId);
        return favoriteRepository.countByMountainId(mountainId);
    }

    /**
     * 사용자의 모든 즐겨찾기 삭제 (회원 탈퇴 시 사용)
     *
     * @param userId 사용자 ID
     */
    public void deleteAllUserFavorites(Long userId) {
        log.info("사용자 모든 즐겨찾기 삭제: userId={}", userId);

        try {
            favoriteRepository.deleteByUserId(userId);
            log.info("사용자 즐겨찾기 전체 삭제 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("사용자 즐겨찾기 삭제 중 오류: userId={}", userId, e);
            throw new RuntimeException("즐겨찾기 삭제 실패", e);
        }
    }

    /**
     * 특정 산을 즐겨찾기한 사용자 ID 목록 조회 (알림 전송용)
     */
    @Transactional(readOnly = true)
    public List<Long> getUserIdsByMountainId(Long mountainId) {
        log.debug("🔍 산 즐겨찾기 사용자 조회: mountainId={}", mountainId);
        return favoriteRepository.findUserIdsByMountainId(mountainId);
    }
}