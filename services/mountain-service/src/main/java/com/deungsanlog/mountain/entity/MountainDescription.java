package com.deungsanlog.mountain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "mountain_descriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MountainDescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mountain_id")
    private Long mountainId;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "full_description", columnDefinition = "TEXT")
    private String fullDescription;

    @Column(name = "nearby_tour_info", columnDefinition = "TEXT")
    private String nearbyTourInfo;

    @Column(name = "difficulty", length = 50)
    private String difficulty;

    @Column(name = "hiking_point_info", columnDefinition = "TEXT")
    private String hikingPointInfo;

    @Column(name = "hiking_course_info", columnDefinition = "TEXT")
    private String hikingCourseInfo;

    @Column(name = "transport_info", columnDefinition = "TEXT")
    private String transportInfo;
}