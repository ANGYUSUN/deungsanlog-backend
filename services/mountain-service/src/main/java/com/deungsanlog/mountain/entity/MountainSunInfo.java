package com.deungsanlog.mountain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "mountain_sun_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MountainSunInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mountain_id")
    private Long mountainId;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "sunrise_time")
    private LocalTime sunriseTime;

    @Column(name = "sunset_time")
    private LocalTime sunsetTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}