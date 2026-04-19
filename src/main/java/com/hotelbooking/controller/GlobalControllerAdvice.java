package com.hotelbooking.controller;

import com.hotelbooking.entity.User;
import com.hotelbooking.service.BookingService;
import com.hotelbooking.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final BookingService bookingService;
    private final SecurityUtils securityUtils;

    @ModelAttribute("unreadClientNotifications")
    public Long unreadClientNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() &&
                auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            User user = securityUtils.getCurrentUser();
            if (user.getRole() == User.Role.CLIENT) {
                return bookingService.getUnreadNotificationsCount(user);
            }
        }
        return 0L;
    }

    @ModelAttribute("unreadAdminNotifications")
    public Long unreadAdminNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            User user = securityUtils.getCurrentUser();
            if (user.getRole() == User.Role.ADMIN) {
                return bookingService.getUnreadAdminNotificationsCount();
            }
        }
        return 0L;
    }

    @ModelAttribute("pendingAdminRequests")
    public Long pendingAdminRequests() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() &&
                auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            User user = securityUtils.getCurrentUser();
            if (user.getRole() == User.Role.ADMIN) {
                return bookingService.getPendingRequestsCount();
            }
        }
        return 0L;
    }
}