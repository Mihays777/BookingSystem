package com.hotelbooking.repository;

import com.hotelbooking.entity.BookingRequest;
import com.hotelbooking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {
    List<BookingRequest> findByClientOrderByRequestDateDesc(User client);
    List<BookingRequest> findAllByOrderByRequestDateDesc();
    List<BookingRequest> findByStatusOrderByRequestDateDesc(BookingRequest.RequestStatus status);
}