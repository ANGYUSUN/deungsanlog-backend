package com.deungsanlog.meeting.repository;

import com.deungsanlog.meeting.entity.MeetingMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingMemberRepository extends JpaRepository<MeetingMember, Long> {
}
