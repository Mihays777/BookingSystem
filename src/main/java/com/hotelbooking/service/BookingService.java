package com.hotelbooking.service;

import com.hotelbooking.entity.*;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.BookingRequestRepository;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRequestRepository requestRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    /**
     * Клиент создает запрос на бронирование.
     * Фиксируется цена номера на момент запроса.
     */
    @Transactional
    public BookingRequest createRequest(Long clientId, Long roomId, LocalDate desiredDate) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.isActive()) {
            throw new RuntimeException("Room is not available for booking");
        }

        // Проверим, нет ли уже подтвержденного бронирования на эту дату для этого номера
        boolean alreadyBooked = requestRepository.findAll().stream()
                .anyMatch(req -> req.getRoom().getId().equals(roomId) &&
                        req.getDesiredDate().equals(desiredDate) &&
                        (req.getStatus() == BookingRequest.RequestStatus.APPROVED ||
                                req.getStatus() == BookingRequest.RequestStatus.PAID));
        if (alreadyBooked) {
            throw new RuntimeException("Room already booked for this date");
        }

        BookingRequest request = new BookingRequest();
        request.setClient(client);
        request.setRoom(room);
        request.setDesiredDate(desiredDate);
        request.setFixedPrice(room.getPrice()); // фиксируем текущую цену
        request.setStatus(BookingRequest.RequestStatus.PENDING);

        return requestRepository.save(request);
    }

    public List<BookingRequest> getClientRequests(Long clientId) {
        User client = userRepository.findById(clientId).orElseThrow();
        return requestRepository.findByClientOrderByRequestDateDesc(client);
    }

    public List<BookingRequest> getAllRequests() {
        return requestRepository.findAllByOrderByRequestDateDesc();
    }

    /**
     * Администратор одобряет запрос.
     * Создается бронирование (Booking) с дедлайном оплаты 24 часа.
     */
    @Transactional
    public void approveRequest(Long requestId) {
        BookingRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != BookingRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be approved");
        }

        request.approve();

        // Создаем бронирование
        Booking booking = new Booking();
        booking.setRequest(request);
        booking.setBookingDate(LocalDateTime.now());
        booking.setPaymentDeadline(LocalDateTime.now().plusHours(24));
        booking.setPaymentStatus(false);

        bookingRepository.save(booking);
        requestRepository.save(request);
    }

    /**
     * Администратор отклоняет запрос с указанием причины.
     */
    @Transactional
    public void rejectRequest(Long requestId, String reason) {
        BookingRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != BookingRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be rejected");
        }

        request.reject(reason);
        requestRepository.save(request);
    }

    /**
     * Клиент подтверждает оплату (имитация).
     */
    @Transactional
    public void confirmPayment(Long bookingId, Long clientId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Проверяем, что бронирование принадлежит клиенту
        if (!booking.getRequest().getClient().getId().equals(clientId)) {
            throw new RuntimeException("Access denied");
        }

        if (booking.isPaymentStatus()) {
            throw new RuntimeException("Already paid");
        }

        if (!booking.checkDeadline()) {
            // Если дедлайн истек, отменяем бронирование и запрос
            cancelExpiredBooking(booking);
            throw new RuntimeException("Payment deadline expired. Booking cancelled.");
        }

        booking.confirmPayment();
        bookingRepository.save(booking);
        // статус запроса обновится в confirmPayment() через каскад?
        // В Booking.confirmPayment() мы меняем статус запроса на PAID
        requestRepository.save(booking.getRequest());
    }

    /**
     * Отмена просроченного бронирования (вызывается планировщиком или при попытке оплаты)
     */
    @Transactional
    public void cancelExpiredBooking(Booking booking) {
        BookingRequest request = booking.getRequest();
        // Отклоняем запрос с причиной "Истек срок оплаты"
        request.setStatus(BookingRequest.RequestStatus.REJECTED);
        request.setRejectionReason("Истек срок оплаты (24 часа)");
        requestRepository.save(request);
        // Удаляем бронирование (или можно оставить с пометкой, но по логике оно больше не нужно)
        bookingRepository.delete(booking);
    }

    /**
     * Планировщик: каждые 30 минут проверяет неоплаченные бронирования с истекшим дедлайном.
     */
    @Scheduled(fixedRate = 1800000) // 30 минут = 1_800_000 мс
    @Transactional
    public void cleanupExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredBookings = bookingRepository.findByPaymentStatusFalseAndPaymentDeadlineBefore(now);
        for (Booking booking : expiredBookings) {
            cancelExpiredBooking(booking);
        }
    }

    public Booking getBookingByRequestId(Long requestId) {
        return bookingRepository.findByRequest(
                requestRepository.findById(requestId).orElseThrow()
        ).orElse(null);
    }

    public List<BookingRequest> getRequestsByStatus(String status) {
        if (status == null || status.isEmpty()) {
            return requestRepository.findAllByOrderByRequestDateDesc();
        }
        try {
            BookingRequest.RequestStatus requestStatus = BookingRequest.RequestStatus.valueOf(status.toUpperCase());
            return requestRepository.findByStatusOrderByRequestDateDesc(requestStatus);
        } catch (IllegalArgumentException e) {
            return requestRepository.findAllByOrderByRequestDateDesc();
        }
    }
}