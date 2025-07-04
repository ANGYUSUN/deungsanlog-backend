package com.deungsanlog.meeting.repository;

import com.deungsanlog.meeting.entity.MeetingMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingMemberRepository extends JpaRepository<MeetingMember, Long> {
    List<MeetingMember> findByMeetingId(Long meetingId);

    // userId로 모든 MeetingMember 조회
    List<MeetingMember> findByUserId(Long userId);

    // userId와 status로 MeetingMember 조회
    List<MeetingMember> findByUserIdAndStatus(Long userId, MeetingMember.Status status);
}
