package com.karim.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to the user who owns this address
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String label;           // "Home", "Work", "Other"

    @Column(nullable = false, length = 100)
    private String name;            // Recipient full name

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, length = 200)
    private String line1;           // Flat / House / Building

    @Column(nullable = false, length = 200)
    private String line2;           // Street / Area / Locality

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(nullable = false, length = 10)
    private String pin;             // PIN / ZIP code

    @Column(length = 200)
    private String landmark;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    // ── GPS coordinates (set when customer pins location on map) ──
    @Column(precision = 10)
    private Double latitude;

    @Column(precision = 10)
    private Double longitude;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ── Convenience: build the formatted string the checkout API expects ──
    public String toFormattedString() {
        StringBuilder sb = new StringBuilder();
        sb.append(line1);
        if (line2 != null && !line2.isBlank()) sb.append(", ").append(line2);
        sb.append(", ").append(city);
        if (state != null && !state.isBlank()) sb.append(", ").append(state);
        sb.append(" – ").append(pin);
        return sb.toString();
    }

    // ── Returns true only if a GPS pin was saved for this address ──
    public boolean hasGpsCoordinates() {
        return latitude != null && longitude != null;
    }
}