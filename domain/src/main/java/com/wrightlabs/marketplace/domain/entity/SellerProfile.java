package com.wrightlabs.marketplace.domain.entity;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import org.hibernate.annotations.Type;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "seller_profiles")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "store_name", nullable = false, length = 255)
    private String storeName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "payout_email", length = 255)
    private String payoutEmail;

    @Column(name = "stripe_account_id", length = 255)
    private String stripeAccountId;

    @Column(nullable = false)
    private Boolean approved = false;

    @Type(ListArrayType.class)
    @Column(columnDefinition = "TEXT[]")
    @Builder.Default
    private List<String> subjects = new ArrayList<>();

    @Type(ListArrayType.class)
    @Column(columnDefinition = "TEXT[]")
    @Builder.Default
    private List<String> grades = new ArrayList<>();

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
