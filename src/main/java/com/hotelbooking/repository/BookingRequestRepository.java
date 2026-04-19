package com.hotelbooking.repository;

import com.hotelbooking.entity.BookingRequest;
import com.hotelbooking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {
    List<BookingRequest> findByClientOrderByRequestDateDesc(User client);
    List<BookingRequest> findAllByOrderByRequestDateDesc();
    List<BookingRequest> findByStatusOrderByRequestDateDesc(BookingRequest.RequestStatus status);

    // Новые методы
    long countByStatus(BookingRequest.RequestStatus status);
    long countByClientAndStatusInAndViewedByClientFalse(User client, List<BookingRequest.RequestStatus> statuses);
    List<BookingRequest> findByClientAndStatusInAndViewedByClientFalse(User client, List<BookingRequest.RequestStatus> statuses);
    // Для админа: количество непросмотренных заявок в статусах PAID и REJECTED (отменённые клиентом)
    long countByStatusInAndViewedByAdminFalse(List<BookingRequest.RequestStatus> statuses);
    List<BookingRequest> findByStatusInAndViewedByAdminFalse(List<BookingRequest.RequestStatus> statuses);

    @Modifying
    @Query("UPDATE BookingRequest r SET r.viewedByClient = true WHERE r.client = :client AND r.status IN :statuses AND r.viewedByClient = false")
    void markAsViewedByClient(@Param("client") User client, @Param("statuses") List<BookingRequest.RequestStatus> statuses);

    @Modifying
    @Query("UPDATE BookingRequest r SET r.viewedByAdmin = true WHERE r.status IN :statuses AND r.viewedByAdmin = false")
    void markAsViewedByAdmin(@Param("statuses") List<BookingRequest.RequestStatus> statuses);
}