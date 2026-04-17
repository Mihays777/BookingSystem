package com.hotelbooking.repository;

import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.BookingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByRequest(BookingRequest request);
    List<Booking> findByPaymentStatusFalseAndPaymentDeadlineBefore(LocalDateTime now);
}