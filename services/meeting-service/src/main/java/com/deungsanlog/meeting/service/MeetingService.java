package com.deungsanlog.meeting.service;

import com.deungsanlog.meeting.dto.MeetingRequestDto;
import com.deungsanlog.meeting.entity.Meeting;
import com.deungsanlog.meeting.entity.MeetingMember;
import com.deungsanlog.meeting.entity.MeetingStatus;
import com.deungsanlog.meeting.exception.BadRequestException;
import com.deungsanlog.meeting.repository.MeetingMemberRepository;
import com.deungsanlog.meeting.repository.MeetingRepository;
import com.deungsanlog.meeting.client.NotificationServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.HashMap;
import java.util.Optional;
import java.util.Map;
import java.util.Comparator;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final NotificationServiceClient notificationServiceClient;

    public Meeting saveMeeting(MeetingRequestDto dto) {
        Meeting meeting = Meeting.builder()
                .hostUserId(dto.getHostUserId())
                .mountainId(dto.getMountainId())
                .mountainName(dto.getMountainName())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .location(dto.getLocation())
                .scheduledDate(dto.getScheduledDate())
                .scheduledTime(dto.getScheduledTime())
                .deadlineDate(dto.getDeadlineDate())
                .gatherLocation(dto.getGatherLocation())
                .maxParticipants(dto.getMaxParticipants())
                .chatLink(dto.getChatLink())
                .status(MeetingStatus.OPEN)
                .build();

        Meeting saved = meetingRepository.save(meeting);

        MeetingMember host = MeetingMember.builder()
                .meetingId(saved.getId())
                .userId(saved.getHostUserId())
                .status(MeetingMember.Status.ACCEPTED)
                .build();

        meetingMemberRepository.save(host);

        return saved;
    }

    public Page<Meeting> getAllMeetings(int page, int size) {
        return meetingRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
    }

    public Page<Meeting> searchMeetings(String status, String sort, String keyword, int page, int size) {
        // 정렬 옵션 결정
        Sort sortOption;
        switch (sort) {
            case "latest":
                sortOption = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
            case "oldest":
                sortOption = Sort.by(Sort.Direction.ASC, "createdAt");
                break;
            case "deadline":
            default:
                sortOption = Sort.by(Sort.Direction.ASC, "deadlineDate");
                break;
        }

        // 키워드 기본 처리
        String keywordValue = keyword == null ? "" : keyword;

        // status 파싱 (Enum으로)
        MeetingStatus meetingStatus = null;
        if (!"all".equalsIgnoreCase(status)) {
            try {
                meetingStatus = MeetingStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("잘못된 status 값: " + status);
            }
        }

        // 쿼리 분기
        Pageable pageable = PageRequest.of(page, size, sortOption);
        if (meetingStatus != null) {
            // 상태 + 제목 검색
            return meetingRepository.findByStatusAndTitleContainingIgnoreCase(
                    meetingStatus, keywordValue, pageable
            );
        } else {
            // 상태 없이 제목 또는 산 이름으로 검색 (새 메서드가 필요함)
            return meetingRepository.findByTitleContainingIgnoreCaseOrMountainNameContainingIgnoreCase(
                    keywordValue, keywordValue, pageable
            );
        }
    }

    public Meeting getMeetingById(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BadRequestException("해당 모임이 존재하지 않습니다."));
    }

    public List<MeetingMember> getMeetingMembers(Long meetingId) {
        return meetingMemberRepository.findByMeetingId(meetingId);
    }

    public List<MeetingMember> getAcceptedMeetingMembers(Long meetingId) {
        return meetingMemberRepository.findByMeetingId(meetingId).stream()
                .filter(m -> m.getStatus() == MeetingMember.Status.ACCEPTED)
                .toList();
    }

    public List<MeetingMember> getPendingApplicants(Long meetingId) {
        return meetingMemberRepository.findByMeetingId(meetingId).stream()
                .filter(m -> m.getStatus() == MeetingMember.Status.PENDING)
                .toList();
    }

    public void applyMeeting(Long meetingId, Long userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BadRequestException("해당 모임이 존재하지 않습니다."));

        MeetingMember member = meetingMemberRepository.findByMeetingId(meetingId).stream()
                .filter(m -> m.getUserId().equals(userId))
                .findFirst()
                .orElse(null);

        if (member == null) {
            // 최초 신청
            MeetingMember newMember = MeetingMember.builder()
                    .meetingId(meetingId)
                    .userId(userId)
                    .status(MeetingMember.Status.PENDING)
                    .build();
            meetingMemberRepository.save(newMember);
            // 알림 전송
            try {
                HashMap<String, Object> req = new HashMap<>();
                req.put("userId", meeting.getHostUserId());
                req.put("type", "meeting_apply");
                req.put("content", "누군가가 [" + meeting.getTitle() + "] 모임에 참가신청을 했습니다.");
                notificationServiceClient.sendNotification(req);
            } catch (Exception e) {
                // 알림 실패는 무시
            }
        } else if (member.getStatus() == MeetingMember.Status.REJECTED || member.getStatus() == MeetingMember.Status.CANCELLED) {
            // 재신청
            member.setStatus(MeetingMember.Status.PENDING);
            meetingMemberRepository.save(member);
        } else if (member.getStatus() == MeetingMember.Status.PENDING || member.getStatus() == MeetingMember.Status.ACCEPTED) {
            throw new BadRequestException("이미 신청 중이거나 참가 중입니다.");
        }
    }

    public void acceptMeetingMember(Long meetingId, Long userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BadRequestException("해당 모임이 존재하지 않습니다."));

        MeetingMember member = meetingMemberRepository.findByMeetingId(meetingId).stream()
                .filter(m -> m.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("신청 내역이 없습니다."));

        if (member.getStatus() != MeetingMember.Status.PENDING) {
            throw new BadRequestException("수락할 수 없는 상태입니다.");
        }

        // 수락 처리
        member.setStatus(MeetingMember.Status.ACCEPTED);
        meetingMemberRepository.save(member);

        // ACCEPTED 인원 수 체크
        long acceptedCount = meetingMemberRepository.findByMeetingId(meetingId).stream()
                .filter(m -> m.getStatus() == MeetingMember.Status.ACCEPTED)
                .count();

        if (acceptedCount >= meeting.getMaxParticipants()) {
            meeting.setStatus(MeetingStatus.FULL);
            meetingRepository.save(meeting);
        }
    }

    public void rejectMeetingMember(Long meetingId, Long userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BadRequestException("해당 모임이 존재하지 않습니다."));

        MeetingMember member = meetingMemberRepository.findByMeetingId(meetingId).stream()
                .filter(m -> m.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("신청 내역이 없습니다."));

        if (member.getStatus() != MeetingMember.Status.PENDING) {
            throw new BadRequestException("거절할 수 없는 상태입니다.");
        }

        // 거절 처리
        member.setStatus(MeetingMember.Status.REJECTED);
        meetingMemberRepository.save(member);
    }

    public void cancelMeetingApplication(Long meetingId, Long userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BadRequestException("해당 모임이 존재하지 않습니다."));

        MeetingMember member = meetingMemberRepository.findByMeetingId(meetingId).stream()
                .filter(m -> m.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("신청 내역이 없습니다."));

        if (member.getStatus() == MeetingMember.Status.PENDING || member.getStatus() == MeetingMember.Status.ACCEPTED) {
            member.setStatus(MeetingMember.Status.CANCELLED);
            meetingMemberRepository.save(member);
        } else {
            throw new BadRequestException("취소할 수 없는 상태입니다.");
        }
    }

    public void closeMeeting(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BadRequestException("해당 모임이 존재하지 않습니다."));
        meeting.setStatus(MeetingStatus.CLOSED);
        meetingRepository.save(meeting);
    }

    public void cancelMeeting(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BadRequestException("해당 모임이 존재하지 않습니다."));
        meeting.setStatus(MeetingStatus.CANCELLED);
        meetingRepository.save(meeting);
    }

    public List<Long> getAcceptedMeetingIdsByUserId(Long userId) {
        return meetingMemberRepository.findByUserIdAndStatus(userId, MeetingMember.Status.ACCEPTED)
                .stream()
                .map(MeetingMember::getMeetingId)
                .distinct()
                .toList();
    }

    public List<Long> getAllMeetingIdsByUserId(Long userId) {
        return meetingMemberRepository.findByUserId(userId)
                .stream()
                .map(MeetingMember::getMeetingId)
                .distinct()
                .toList();
    }

    public List<Long> getHostedMeetingIdsByUserId(Long userId) {
        return meetingRepository.findByHostUserId(userId)
                .stream()
                .map(Meeting::getId)
                .toList();
    }

    public List<Long> getMeetingIdsByUserIdAndStatus(Long userId, String status) {
        if ("all".equalsIgnoreCase(status)) {
            return getAcceptedMeetingIdsByUserId(userId);
        }
        
        MeetingStatus meetingStatus = MeetingStatus.valueOf(status.toUpperCase());
        return meetingMemberRepository.findByUserIdAndStatus(userId, MeetingMember.Status.ACCEPTED)
                .stream()
                .map(MeetingMember::getMeetingId)
                .distinct()
                .map(meetingRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(meeting -> meeting.getStatus() == meetingStatus)
                .map(Meeting::getId)
                .toList();
    }

    public List<Long> getHostedMeetingIdsByUserIdAndStatus(Long userId, String status) {
        if ("all".equalsIgnoreCase(status)) {
            return getHostedMeetingIdsByUserId(userId);
        }
        
        MeetingStatus meetingStatus = MeetingStatus.valueOf(status.toUpperCase());
        return meetingRepository.findByHostUserId(userId)
                .stream()
                .filter(meeting -> meeting.getStatus() == meetingStatus)
                .map(Meeting::getId)
                .toList();
    }

    public Map<String, Object> getMyMeetingsFiltered(Long userId, String type, String status, String sort, 
                                                   String startDate, String endDate, int page, int size) {
        // 1. 모임 ID 목록 가져오기
        List<Long> meetingIds;
        if ("hosted".equals(type)) {
            meetingIds = getHostedMeetingIdsByUserIdAndStatus(userId, status);
        } else {
            meetingIds = getMeetingIdsByUserIdAndStatus(userId, status);
        }

        if (meetingIds.isEmpty()) {
            return Map.of(
                "meetings", List.of(),
                "totalElements", 0L,
                "totalPages", 0,
                "currentPage", page,
                "size", size
            );
        }

        // 2. 모임 상세 정보 가져오기
        List<Meeting> meetings = meetingRepository.findAllById(meetingIds);

        // 3. 날짜 필터링
        if (startDate != null && !startDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            meetings = meetings.stream()
                    .filter(meeting -> meeting.getScheduledDate().isAfter(start.minusDays(1)))
                    .toList();
        }

        if (endDate != null && !endDate.isEmpty()) {
            LocalDate end = LocalDate.parse(endDate);
            meetings = meetings.stream()
                    .filter(meeting -> meeting.getScheduledDate().isBefore(end.plusDays(1)))
                    .toList();
        }

        // 4. 정렬
        switch (sort) {
            case "oldest":
                meetings = meetings.stream()
                        .sorted(Comparator.comparing(Meeting::getScheduledDate)
                                .thenComparing(Meeting::getScheduledTime))
                        .toList();
                break;
            case "deadline":
                meetings = meetings.stream()
                        .sorted(Comparator.comparing(Meeting::getDeadlineDate))
                        .toList();
                break;
            case "latest":
            default:
                meetings = meetings.stream()
                        .sorted(Comparator.comparing(Meeting::getScheduledDate)
                                .thenComparing(Meeting::getScheduledTime)
                                .reversed())
                        .toList();
                break;
        }

        // 5. 페이지네이션
        int totalElements = meetings.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);
        
        List<Meeting> pagedMeetings = startIndex < totalElements ? 
                meetings.subList(startIndex, endIndex) : List.of();

        return Map.of(
            "meetings", pagedMeetings,
            "totalElements", (long) totalElements,
            "totalPages", totalPages,
            "currentPage", page,
            "size", size
        );
    }
}