package com.smartstay.controller;

import com.smartstay.config.AuthInterceptor;
import com.smartstay.dto.ReservationRequestDto;
import com.smartstay.dto.UserSession;
import com.smartstay.exception.BookingConflictException;
import com.smartstay.exception.ValidationException;
import com.smartstay.model.Guest;
import com.smartstay.model.Promotion;
import com.smartstay.model.Reservation;
import com.smartstay.model.Room;
import com.smartstay.repository.GuestRepository;
import com.smartstay.service.PromotionService;
import com.smartstay.service.ReservationService;
import com.smartstay.service.RoomService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class ReservationController {

    private final ReservationService reservationService;
    private final RoomService roomService;
    private final GuestRepository guestRepository;
    private final PromotionService promotionService;

    public ReservationController(ReservationService reservationService,
                                 RoomService roomService,
                                 GuestRepository guestRepository,
                                 PromotionService promotionService) {
        this.reservationService = reservationService;
        this.roomService = roomService;
        this.guestRepository = guestRepository;
        this.promotionService = promotionService;
    }

    @GetMapping("/reservations/book/{roomId}")
    public String showBookingForm(@PathVariable("roomId") Integer roomId,
                                  @RequestParam(value = "checkIn", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkIn,
                                  @RequestParam(value = "checkOut", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOut,
                                  HttpSession session,
                                  Model model) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) {
            return "redirect:/login?message=Please log in to reserve a room";
        }
        // Allow guests AND reservation officers (staff with RESERVATIONS role or manager) to book
        if (!currentUser.isGuest() && !currentUser.isReservations() && !currentUser.isStaff()) {
            return "redirect:/dashboard?error=Access Denied: Only guests or reservation officers can make bookings";
        }

        Room room = roomService.getRoomById(roomId);
        ReservationRequestDto dto = new ReservationRequestDto();
        dto.setRoomId(roomId);
        dto.setCheckInDate(checkIn != null ? checkIn : LocalDate.now());
        dto.setCheckOutDate(checkOut != null ? checkOut : LocalDate.now().plusDays(1));

        if (currentUser.isGuest()) {
            guestRepository.findById(currentUser.getId()).ifPresent(g -> {
                dto.setGuestId(g.getGuestId());
                dto.setGuestName(g.getName());
                dto.setGuestEmail(g.getEmail());
                dto.setGuestPhone(g.getPhone());
            });
        }

        List<Promotion> activePromotions = promotionService.getActivePromotions();

        model.addAttribute("room", room);
        model.addAttribute("reservationDto", dto);
        model.addAttribute("activePromotions", activePromotions);
        model.addAttribute("currentUser", currentUser);
        return "reservations/book";
    }

    @PostMapping("/reservations/create")
    public String createReservation(@Valid @ModelAttribute("reservationDto") ReservationRequestDto dto,
                                    BindingResult bindingResult,
                                    HttpSession session,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";

        if (bindingResult.hasErrors()) {
            Room room = roomService.getRoomById(dto.getRoomId());
            model.addAttribute("room", room);
            model.addAttribute("activePromotions", promotionService.getActivePromotions());
            model.addAttribute("currentUser", currentUser);
            return "reservations/book";
        }

        try {
            Reservation reservation = reservationService.createReservation(dto, currentUser.getId(), currentUser.isStaff());
            redirectAttributes.addFlashAttribute("successMessage", "Reservation #" + reservation.getReservationId() + " created successfully!");
            return "redirect:/reservations/confirmation/" + reservation.getReservationId();
        } catch (BookingConflictException | ValidationException e) {
            Room room = roomService.getRoomById(dto.getRoomId());
            model.addAttribute("room", room);
            model.addAttribute("activePromotions", promotionService.getActivePromotions());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("errorMessage", e.getMessage());
            return "reservations/book";
        }
    }

    @GetMapping("/reservations/confirmation/{id}")
    public String bookingConfirmation(@PathVariable("id") Integer id, Model model) {
        Reservation res = reservationService.getReservationById(id);
        model.addAttribute("reservation", res);
        return "reservations/confirmation";
    }

    @GetMapping("/reservations/details/{id}")
    public String reservationDetails(@PathVariable("id") Integer id, Model model) {
        Reservation res = reservationService.getReservationById(id);
        model.addAttribute("reservation", res);
        return "reservations/details";
    }

    @GetMapping("/guest/reservations")
    public String guestReservations(HttpSession session, Model model) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null || !currentUser.isGuest()) return "redirect:/login";

        List<Reservation> reservations = reservationService.getReservationsByGuest(currentUser.getId());
        model.addAttribute("reservations", reservations);
        return "reservations/my-reservations";
    }

    // Staff Reservations View
    @GetMapping("/staff/reservations")
    public String staffReservations(HttpSession session, Model model) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isStaff()) return "redirect:/dashboard?error=Access Denied: Staff role required";

        List<Reservation> reservations = reservationService.getAllReservations();
        List<Room> rooms = roomService.getAllRooms();
        List<Guest> guests = guestRepository.findAll();

        model.addAttribute("reservations", reservations);
        model.addAttribute("rooms", rooms);
        model.addAttribute("guests", guests);
        return "reservations/manage";
    }

    @PostMapping("/staff/reservations/check-in/{id}")
    public String checkIn(@PathVariable("id") Integer id, HttpSession session, RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isStaff()) return "redirect:/dashboard?error=Access Denied: Staff role required";

        reservationService.checkIn(id, currentUser.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Guest successfully checked in for Reservation #" + id);
        return "redirect:/staff/reservations";
    }

    @PostMapping("/staff/reservations/check-out/{id}")
    public String checkOut(@PathVariable("id") Integer id, HttpSession session, RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isStaff()) return "redirect:/dashboard?error=Access Denied: Staff role required";

        reservationService.checkOut(id, currentUser.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Guest successfully checked out for Reservation #" + id);
        return "redirect:/staff/reservations";
    }

    @PostMapping("/reservations/cancel/{id}")
    public String cancelReservation(@PathVariable("id") Integer id,
                                    @RequestParam(value = "reason", required = false) String reason,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";

        try {
            reservationService.cancelReservation(id, reason, currentUser.getId(), currentUser.isStaff());
            redirectAttributes.addFlashAttribute("successMessage", "Reservation #" + id + " has been successfully cancelled.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to cancel reservation: " + e.getMessage());
            return "redirect:/reservations/details/" + id;
        }

        if (currentUser.isStaff()) {
            return "redirect:/staff/reservations";
        } else {
            return "redirect:/guest/reservations";
        }
    }

    @PostMapping("/reservations/modify/{id}")
    public String modifyReservation(@PathVariable("id") Integer id,
                                    @RequestParam("checkInDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkIn,
                                    @RequestParam("checkOutDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOut,
                                    @RequestParam(value = "numGuests", defaultValue = "1") int numGuests,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";

        try {
            reservationService.modifyReservation(id, checkIn, checkOut, numGuests, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Reservation #" + id + " modified successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/reservations/details/" + id;
    }

    @PostMapping("/staff/reservations/delete/{id}")
    public String deleteReservation(@PathVariable("id") Integer id,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isIt() && !currentUser.isManager()) {
            return "redirect:/dashboard?error=Access Denied: Admin role required to delete reservations";
        }

        try {
            reservationService.deleteReservation(id, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Reservation #" + id + " permanently deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete reservation: " + e.getMessage());
        }
        return "redirect:/staff/reservations";
    }

    @PostMapping("/reservations/special-requests/remove/{requestId}")
    public String removeSpecialRequest(@PathVariable("requestId") Integer requestId,
                                       @RequestParam("reservationId") Integer reservationId,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";

        try {
            reservationService.removeSpecialRequest(requestId, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Special request removed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/reservations/details/" + reservationId;
    }
}
