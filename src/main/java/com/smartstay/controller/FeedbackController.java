package com.smartstay.controller;

import com.smartstay.config.AuthInterceptor;
import com.smartstay.dto.UserSession;
import com.smartstay.model.Feedback;
import com.smartstay.model.Refund;
import com.smartstay.model.Reservation;
import com.smartstay.service.FeedbackService;
import com.smartstay.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final ReservationService reservationService;

    public FeedbackController(FeedbackService feedbackService, ReservationService reservationService) {
        this.feedbackService = feedbackService;
        this.reservationService = reservationService;
    }

    @GetMapping("/feedback/submit/{reservationId}")
    public String showFeedbackForm(@PathVariable("reservationId") Integer reservationId, Model model) {
        Reservation reservation = reservationService.getReservationById(reservationId);
        model.addAttribute("reservation", reservation);
        return "feedback/submit";
    }

    @PostMapping("/feedback/create")
    public String submitFeedback(@RequestParam("reservationId") Integer reservationId,
                                 @RequestParam(value = "rating", defaultValue = "5") Integer rating,
                                 @RequestParam(value = "category", defaultValue = "SERVICE") String category,
                                 @RequestParam("comments") String comments,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";

        try {
            Reservation reservation = reservationService.getReservationById(reservationId);
            Integer guestId = currentUser.isGuest() ? currentUser.getId() : (reservation != null ? reservation.getGuestId() : currentUser.getId());
            if (guestId == null && reservation != null) {
                guestId = reservation.getGuestId();
            }

            // Ensure rating is valid (1-5)
            int validRating = (rating != null) ? Math.max(1, Math.min(5, rating)) : 5;

            // Ensure comments length does not exceed DB limit (1000 chars)
            String safeComments = (comments != null && comments.length() > 1000) ? comments.substring(0, 1000) : comments;

            feedbackService.submitFeedback(guestId, reservationId, validRating, category, safeComments);
            redirectAttributes.addFlashAttribute("successMessage", "Thank you for your feedback! Our guest relations team has received your ticket.");
            
            if (currentUser.isGuest()) {
                return "redirect:/guest/feedback";
            } else {
                return "redirect:/staff/feedback";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not submit feedback: " + e.getMessage());
            return "redirect:/feedback/submit/" + reservationId;
        }
    }

    @GetMapping("/guest/feedback")
    public String guestFeedbackList(HttpSession session, Model model) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null || !currentUser.isGuest()) return "redirect:/login";

        List<Feedback> feedbackList = feedbackService.getFeedbackByGuest(currentUser.getId());
        model.addAttribute("feedbackList", feedbackList);
        return "feedback/list";
    }

    @PostMapping("/guest/feedback/edit/{feedbackId}")
    public String editFeedback(@PathVariable("feedbackId") Integer feedbackId,
                               @RequestParam(value = "rating", defaultValue = "5") Integer rating,
                               @RequestParam("category") String category,
                               @RequestParam("comments") String comments,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null || !currentUser.isGuest()) return "redirect:/login";

        try {
            feedbackService.editFeedback(feedbackId, rating, category, comments, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Feedback updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/guest/feedback";
    }

    @PostMapping("/guest/feedback/delete/{feedbackId}")
    public String deleteGuestFeedback(@PathVariable("feedbackId") Integer feedbackId,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null || !currentUser.isGuest()) return "redirect:/login";

        try {
            feedbackService.deleteFeedbackByGuest(feedbackId, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Feedback ticket #" + feedbackId + " deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete feedback: " + e.getMessage());
        }
        return "redirect:/guest/feedback";
    }

    // Staff Feedback Management
    @GetMapping("/staff/feedback")
    public String staffFeedbackList(@RequestParam(value = "search", required = false) String search,
                                    HttpSession session, Model model) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isStaff()) return "redirect:/dashboard?error=Access Denied: Staff role required";

        List<Feedback> allFeedback = feedbackService.searchFeedback(search);
        model.addAttribute("feedbackList", allFeedback);
        model.addAttribute("search", search);
        return "feedback/manage";
    }

    @PostMapping("/staff/feedback/update-status")
    public String updateStatus(@RequestParam("feedbackId") Integer feedbackId,
                               @RequestParam("status") String status,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isStaff()) return "redirect:/dashboard?error=Access Denied: Staff role required";

        feedbackService.updateFeedbackStatus(feedbackId, status, currentUser.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Feedback #" + feedbackId + " status updated to " + status);
        return "redirect:/staff/feedback";
    }

    @PostMapping("/staff/feedback/respond")
    public String addStaffResponse(@RequestParam("feedbackId") Integer feedbackId,
                                   @RequestParam("response") String response,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isStaff()) return "redirect:/dashboard?error=Access Denied: Staff role required";

        try {
            feedbackService.addStaffResponse(feedbackId, response, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Response added to Feedback #" + feedbackId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/staff/feedback";
    }

    @PostMapping("/staff/feedback/delete/{feedbackId}")
    public String deleteFeedback(@PathVariable("feedbackId") Integer feedbackId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isIt() && !currentUser.isManager() && !currentUser.isGuestRelations()) {
            return "redirect:/dashboard?error=Access Denied: Staff role required to delete feedback";
        }

        try {
            feedbackService.deleteFeedback(feedbackId, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Feedback #" + feedbackId + " deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete feedback: " + e.getMessage());
        }
        return "redirect:/staff/feedback";
    }

    @PostMapping("/staff/feedback/refund")
    public String createRefund(@RequestParam("feedbackId") Integer feedbackId,
                               @RequestParam("amount") BigDecimal amount,
                               @RequestParam("reason") String reason,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isStaff()) return "redirect:/dashboard?error=Access Denied: Staff role required";

        try {
            feedbackService.issueRefundForFeedback(feedbackId, amount, reason, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Refund request of $" + amount + " submitted for approval.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/staff/feedback";
    }

    @GetMapping("/staff/refunds")
    public String staffRefunds(HttpSession session, Model model) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isStaff()) return "redirect:/dashboard?error=Access Denied: Staff role required";

        List<Refund> refunds = feedbackService.getAllRefunds();
        model.addAttribute("refunds", refunds);
        return "feedback/refunds";
    }

    @PostMapping("/staff/refunds/decision")
    public String decideRefund(@RequestParam("refundId") Integer refundId,
                               @RequestParam("decision") String decision, // APPROVED, REJECTED, COMPLETED
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isStaff()) return "redirect:/dashboard?error=Access Denied: Staff role required";

        try {
            feedbackService.processRefundDecision(refundId, decision, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Refund #" + refundId + " marked as " + decision);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/staff/refunds";
    }
}
