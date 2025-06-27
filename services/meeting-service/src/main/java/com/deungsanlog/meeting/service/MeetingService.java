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
                .status(MeetingMember.Status.ACCEPTED)
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

    public Meeting getMeetingById(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BadRequestException("해당 모임이 존재하지 않습니다."));
    }

    public List<MeetingMember> getMeetingMembers(Long meetingId) {
        return meetingMemberRepository.findByMeetingId(meetingId);
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
}