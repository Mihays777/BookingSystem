package com.hotelbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime requestDate;

    @Column(nullable = false)
    private LocalDate desiredDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fixedPrice; // цена на момент запроса

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(length = 500)
    private String rejectionReason;

    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL)
    private Booking booking;

    public enum RequestStatus {
        PENDING, APPROVED, REJECTED, PAID
    }

    // Вспомогательные методы
    public void approve() {
        this.status = RequestStatus.APPROVED;
    }

    public void reject(String reason) {
        this.status = RequestStatus.REJECTED;
        this.rejectionReason = reason;
    }
}