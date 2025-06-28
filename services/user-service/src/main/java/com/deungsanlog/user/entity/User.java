package com.deungsanlog.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_provider_user", columnNames = {"provider", "provider_id"}),
                @UniqueConstraint(name = "email", columnNames = {"email"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", length = 100, unique = true)
    private String email;

    @Column(name = "nickname", length = 50, nullable = false)
    private String nickname;

    @Column(name = "profile_img_url", length = 255)
    private String profileImgUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "provider", length = 50, nullable = false)
    private String provider;

    @Column(name = "provider_id", length = 100, nullable = false)
    private String providerId;

    @Column(name = "fcm_token", length = 500)
    private String fcmToken;

    @Column(name = "fcm_token_updated_at")
    private LocalDateTime fcmTokenUpdatedAt;

    // 커스텀 생성자 (소셜 로그인용)
    public User(String email, String nickname, String profileImgUrl, String provider, String providerId) {
        this.email = email;
        this.nickname = nickname;
        this.profileImgUrl = profileImgUrl;
        this.provider = provider;
        this.providerId = providerId;
    }
}