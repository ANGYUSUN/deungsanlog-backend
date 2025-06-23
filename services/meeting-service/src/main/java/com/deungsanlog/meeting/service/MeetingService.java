package com.deungsanlog.meeting.service;

import com.deungsanlog.meeting.dto.MeetingRequestDto;
import com.deungsanlog.meeting.entity.Meeting;
import com.deungsanlog.meeting.entity.MeetingMember;
import com.deungsanlog.meeting.entity.MeetingStatus;
import com.deungsanlog.meeting.exception.BadRequestException;
import com.deungsanlog.meeting.repository.MeetingMemberRepository;
import com.deungsanlog.meeting.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingMemberRepository meetingMemberRepository;

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
                .status(MeetingMember.Status.JOINED)
                .build();

        meetingMemberRepository.save(host);

        return saved;
    }

    public Page<Meeting> getAllMeetings(int page) {
        return meetingRepository.findAll(
                PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
    }

    public Page<Meeting> searchMeetings(String status, String sort, String keyword, int page) {
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
        Pageable pageable = PageRequest.of(page, 10, sortOption);
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


    public List<MeetingMember> getMeetingMembers(Long meetingId) {
        return meetingMemberRepository.findByMeetingId(meetingId);
    }
}
