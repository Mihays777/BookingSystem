package com.hotelbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false, unique = true)
    private BookingRequest request;

    @Column(nullable = false)
    private LocalDateTime bookingDate = LocalDateTime.now();

    @Column(nullable = false)
    private boolean paymentStatus = false;

    @Column(nullable = false)
    private LocalDateTime paymentDeadline; // дата и время, до которого нужно оплатить

    // Методы
    public void confirmPayment() {
        this.paymentStatus = true;
        this.request.setStatus(BookingRequest.RequestStatus.PAID);
    }

    public boolean checkDeadline() {
        return LocalDateTime.now().isBefore(paymentDeadline);
    }
}