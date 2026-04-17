package com.hotelbooking.controller;

import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.BookingRequest;
import com.hotelbooking.entity.Room;
import com.hotelbooking.entity.User;
import com.hotelbooking.service.BookingService;
import com.hotelbooking.service.RoomService;
import com.hotelbooking.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class ClientController {

    private final RoomService roomService;
    private final BookingService bookingService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String listRooms(Model model) {
        User user = securityUtils.getCurrentUser();
        List<Room> rooms = roomService.getActiveRooms();
        model.addAttribute("rooms", rooms);
        model.addAttribute("user", user);
        return "client/rooms";
    }

    @GetMapping("/{id}/request")
    public String requestForm(@PathVariable Long id, Model model) {
        Room room = roomService.getRoomById(id);
        if (!room.isActive()) {
            return "redirect:/rooms";
        }
        model.addAttribute("room", room);
        model.addAttribute("desiredDate", LocalDate.now().plusDays(1));
        return "client/request-form";
    }

    @PostMapping("/{id}/request")
    public String createRequest(@PathVariable Long id,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desiredDate,
                                RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser();
        try {
            bookingService.createRequest(user.getId(), id, desiredDate);
            redirectAttributes.addFlashAttribute("success", "Запрос на бронирование отправлен");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rooms";
    }

    @GetMapping("/requests")
    public String myRequests(Model model) {
        User user = securityUtils.getCurrentUser();
        List<BookingRequest> requests = bookingService.getClientRequests(user.getId());
        model.addAttribute("requests", requests);
        return "client/requests";
    }

    @GetMapping("/requests/{requestId}/pay")
    public String payPage(@PathVariable Long requestId, Model model, RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser();
        BookingRequest request = bookingService.getAllRequests().stream()
                .filter(r -> r.getId().equals(requestId))
                .findFirst().orElse(null);
        if (request == null || !request.getClient().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Запрос не найден");
            return "redirect:/rooms/requests";
        }
        if (request.getStatus() != BookingRequest.RequestStatus.APPROVED) {
            redirectAttributes.addFlashAttribute("error", "Оплата возможна только для одобренных заявок");
            return "redirect:/rooms/requests";
        }
        Booking booking = bookingService.getBookingByRequestId(requestId);
        if (booking == null || booking.isPaymentStatus()) {
            redirectAttributes.addFlashAttribute("error", "Бронирование не найдено или уже оплачено");
            return "redirect:/rooms/requests";
        }
        model.addAttribute("booking", booking);
        model.addAttribute("request", request);
        return "client/payment";
    }

    @PostMapping("/requests/{requestId}/pay")
    public String confirmPayment(@PathVariable Long requestId, RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser();
        try {
            Booking booking = bookingService.getBookingByRequestId(requestId);
            if (booking == null) {
                redirectAttributes.addFlashAttribute("error", "Бронирование не найдено");
                return "redirect:/rooms/requests";
            }
            bookingService.confirmPayment(booking.getId(), user.getId());
            redirectAttributes.addFlashAttribute("success", "Оплата прошла успешно!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rooms/requests";
    }
}