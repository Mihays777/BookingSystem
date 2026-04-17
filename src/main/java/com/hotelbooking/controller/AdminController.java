package com.hotelbooking.controller;

import com.hotelbooking.entity.BookingRequest;
import com.hotelbooking.entity.Room;
import com.hotelbooking.service.BookingService;
import com.hotelbooking.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final RoomService roomService;
    private final BookingService bookingService;

    // Управление номерами
    @GetMapping("/rooms")
    public String listRooms(Model model) {
        List<Room> rooms = roomService.getAllRooms();
        long activeCount = rooms.stream().filter(Room::isActive).count();
        long inactiveCount = rooms.size() - activeCount;
        double avgPrice = rooms.stream().mapToDouble(r -> r.getPrice().doubleValue()).average().orElse(0.0);

        model.addAttribute("rooms", rooms);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", inactiveCount);
        model.addAttribute("avgPrice", avgPrice);
        return "admin/rooms";
    }

    @GetMapping("/rooms/new")
    public String newRoomForm(Model model) {
        model.addAttribute("room", new Room());
        return "admin/room-form";
    }

    @PostMapping("/rooms")
    public String createRoom(@RequestParam String description,
                             @RequestParam BigDecimal price,
                             @RequestParam("photo") MultipartFile photo,
                             RedirectAttributes redirectAttributes) {
        try {
            roomService.createRoom(description, price, photo);
            redirectAttributes.addFlashAttribute("success", "Номер добавлен");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка загрузки фото");
        }
        return "redirect:/admin/rooms";
    }

    @GetMapping("/rooms/{id}/edit")
    public String editRoomForm(@PathVariable Long id, Model model) {
        Room room = roomService.getRoomById(id);
        model.addAttribute("room", room);
        return "admin/room-edit";
    }

    @PostMapping("/rooms/{id}")
    public String updateRoom(@PathVariable Long id,
                             @RequestParam String description,
                             @RequestParam BigDecimal price,
                             @RequestParam(required = false) Boolean isActive,
                             @RequestParam(value = "photo", required = false) MultipartFile photo,
                             RedirectAttributes redirectAttributes) {
        try {
            // Если чекбокс не отмечен, параметр isActive = null -> значит false
            boolean activeStatus = isActive != null ? isActive : false;
            roomService.updateRoom(id, description, price, activeStatus, photo);
            redirectAttributes.addFlashAttribute("success", "Номер обновлён");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка загрузки фото");
        }
        return "redirect:/admin/rooms";
    }

    @PostMapping("/rooms/{id}/delete")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        roomService.deleteRoom(id);
        redirectAttributes.addFlashAttribute("success", "Номер удалён");
        return "redirect:/admin/rooms";
    }

    // Управление запросами
    @GetMapping("/requests")
    public String listRequests(@RequestParam(required = false) String status, Model model) {
        List<BookingRequest> requests = bookingService.getRequestsByStatus(status);
        model.addAttribute("requests", requests);
        model.addAttribute("currentStatus", status);
        return "admin/requests";
    }

    @PostMapping("/requests/{id}/approve")
    public String approveRequest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.approveRequest(id);
            redirectAttributes.addFlashAttribute("success", "Запрос одобрен");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/requests";
    }

    @PostMapping("/requests/{id}/reject")
    public String rejectRequest(@PathVariable Long id,
                                @RequestParam String reason,
                                RedirectAttributes redirectAttributes) {
        try {
            bookingService.rejectRequest(id, reason);
            redirectAttributes.addFlashAttribute("success", "Запрос отклонён");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/requests";
    }
}