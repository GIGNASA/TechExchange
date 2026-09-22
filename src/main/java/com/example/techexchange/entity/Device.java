package com.example.techexchange.entity;

import com.example.techexchange.entity.enums.DeviceCategory;
import com.example.techexchange.entity.enums.DeviceCondition;
import com.example.techexchange.entity.enums.DeviceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 180)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DeviceCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_condition", nullable = false, length = 32)
    private DeviceCondition condition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private DeviceStatus status = DeviceStatus.AVAILABLE;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(length = 120)
    private String brand;

    @Column(length = 120)
    private String model;

    @Column(length = 120)
    private String city;

    @Column(name = "desired_exchange", length = 280)
    private String desiredExchange;

    @Column(length = 1400)
    private String description;

    @Column(name = "image_url", length = 700)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
