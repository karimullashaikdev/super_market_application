package com.karim.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.karim.enums.OrderStatus;
import com.karim.enums.PaymentStatus;
import com.karim.enums.PaymentType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<OrderItem> items;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.deleted   = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private String address;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private Long deliveryAgentId;
    private String otp;

    // Customer delivery address coordinates
    private Double latitude;
    private Double longitude;

    // Structured address parts for geocoding
    private String addressLine2;
    private String addressCity;
    private String addressState;
    private String addressPin;

    // ✅ Agent's live location — updated every 5s while out for delivery
    private Double agentLatitude;
    private Double agentLongitude;
}