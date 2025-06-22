package com.deungsanlog.meeting.service;

import com.deungsanlog.meeting.dto.MeetingRequestDto;
import com.deungsanlog.meeting.entity.Meeting;
import com.deungsanlog.meeting.entity.MeetingMember;
import com.deungsanlog.meeting.entity.MeetingStatus;
import com.deungsanlog.meeting.repository.MeetingMemberRepository;
import com.deungsanlog.meeting.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    public List<MeetingMember> getMeetingMembers(Long meetingId) {
        return meetingMemberRepository.findByMeetingId(meetingId);
    }
}
