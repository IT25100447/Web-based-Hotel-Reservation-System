package com.smartstay.controller;

import com.smartstay.config.AuthInterceptor;
import com.smartstay.dto.DashboardStatsDto;
import com.smartstay.dto.UserSession;
import com.smartstay.model.Feedback;
import com.smartstay.model.Payment;
import com.smartstay.model.Promotion;
import com.smartstay.model.Reservation;
import com.smartstay.service.FeedbackService;
import com.smartstay.service.PaymentService;
import com.smartstay.service.PromotionService;
import com.smartstay.service.ReportService;
import com.smartstay.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    private final ReservationService reservationService;
    private final PaymentService paymentService;
    private final FeedbackService feedbackService;
    private final ReportService reportService;
    private final PromotionService promotionService;

    public DashboardController(ReservationService reservationService,
                               PaymentService paymentService,
                               FeedbackService feedbackService,
                               ReportService reportService,
                               PromotionService promotionService) {
        this.reservationService = reservationService;
        this.paymentService = paymentService;
        this.feedbackService = feedbackService;
        this.reportService = reportService;
        this.promotionService = promotionService;
    }

    @GetMapping("/dashboard")
    public String viewDashboard(HttpSession session, Model model) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";

        if (currentUser.isGuest()) {
            List<Reservation> reservations = reservationService.getReservationsByGuest(currentUser.getId());
            List<Payment> payments = paymentService.getPaymentsByGuest(currentUser.getId());
            List<Feedback> feedbacks = feedbackService.getFeedbackByGuest(currentUser.getId());

            model.addAttribute("reservations", reservations);
            model.addAttribute("payments", payments);
            model.addAttribute("feedbacks", feedbacks);
            return "dashboard/guest";
        }

        // Staff & Manager
        DashboardStatsDto stats = reportService.getManagerDashboardStats();
        model.addAttribute("stats", stats);

        if (currentUser.isManager()) {
            List<Promotion> promotions = promotionService.getAllPromotions();
            model.addAttribute("promotions", promotions);
            model.addAttribute("activeTab", "dashboard");
            return "dashboard/manager";
        } else if (currentUser.isIt()) {
            return "redirect:/staff/it/users";
        } else {
            return "dashboard/staff";
        }
    }

    @GetMapping("/manager/reports")
    public String viewReports(HttpSession session, Model model) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null || !currentUser.isManager()) {
            return "redirect:/dashboard";
        }

        DashboardStatsDto stats = reportService.getManagerDashboardStats();
        List<Promotion> promotions = promotionService.getAllPromotions();
        model.addAttribute("stats", stats);
        model.addAttribute("promotions", promotions);
        model.addAttribute("activeTab", "reports");
        return "dashboard/manager";
    }

    @GetMapping("/manager/promotions")
    public String viewPromotions(HttpSession session, Model model) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null || !currentUser.isManager()) {
            return "redirect:/dashboard";
        }

        DashboardStatsDto stats = reportService.getManagerDashboardStats();
        List<Promotion> promotions = promotionService.getAllPromotions();
        model.addAttribute("stats", stats);
        model.addAttribute("promotions", promotions);
        model.addAttribute("activeTab", "promotions");
        return "dashboard/manager";
    }

    // ===== Promotion CRUD (General Manager only) =====

    @PostMapping("/manager/promotions/create")
    public String createPromotion(@RequestParam("name") String name,
                                  @RequestParam("discountPct") BigDecimal discountPct,
                                  @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                  @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null || !currentUser.isManager()) {
            return "redirect:/login";
        }

        try {
            Promotion promo = new Promotion();
            promo.setName(name);
            promo.setDiscountPct(discountPct);
            promo.setStartDate(startDate);
            promo.setEndDate(endDate);
            promotionService.createPromotion(promo, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Promotion \"" + name + "\" (" + discountPct + "% off) created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create promotion: " + e.getMessage());
        }
        return "redirect:/manager/promotions";
    }

    @PostMapping("/manager/promotions/delete/{promoId}")
    public String deletePromotion(@PathVariable("promoId") Integer promoId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null || !currentUser.isManager()) {
            return "redirect:/login";
        }

        try {
            promotionService.deletePromotion(promoId, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Promotion deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete promotion: " + e.getMessage());
        }
        return "redirect:/manager/promotions";
    }
}
