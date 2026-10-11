package com.smartstay.controller;

import com.smartstay.config.AuthInterceptor;
import com.smartstay.dto.PaymentRequestDto;
import com.smartstay.dto.UserSession;
import com.smartstay.model.Invoice;
import com.smartstay.model.Payment;
import com.smartstay.model.Reservation;
import com.smartstay.service.PaymentService;
import com.smartstay.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class PaymentController {

    private final PaymentService paymentService;
    private final ReservationService reservationService;

    public PaymentController(PaymentService paymentService, ReservationService reservationService) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
    }

    @GetMapping("/payments/checkout/{reservationId}")
    public String showCheckoutPage(@PathVariable("reservationId") Integer reservationId, Model model) {
        Reservation reservation = reservationService.getReservationById(reservationId);
        Invoice invoice = paymentService.getInvoiceByReservation(reservationId);

        PaymentRequestDto dto = new PaymentRequestDto();
        dto.setReservationId(reservationId);
        dto.setAmount(invoice.getOutstandingBalance());
        dto.setMethod("CARD");

        model.addAttribute("reservation", reservation);
        model.addAttribute("invoice", invoice);
        model.addAttribute("paymentDto", dto);
        return "payments/checkout";
    }

    @PostMapping("/payments/process")
    public String processPayment(@Valid @ModelAttribute("paymentDto") PaymentRequestDto dto,
                                 BindingResult bindingResult,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";

        if (bindingResult.hasErrors()) {
            Reservation reservation = reservationService.getReservationById(dto.getReservationId());
            Invoice invoice = paymentService.getInvoiceByReservation(dto.getReservationId());
            model.addAttribute("reservation", reservation);
            model.addAttribute("invoice", invoice);
            return "payments/checkout";
        }

        try {
            Payment payment = paymentService.recordPayment(dto, currentUser.getId(), currentUser.isStaff());
            if ("PENDING".equalsIgnoreCase(payment.getStatus())) {
                redirectAttributes.addFlashAttribute("infoMessage", "Payment submitted! Bank transfer receipt is awaiting staff verification.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Payment of $" + payment.getAmount() + " processed successfully!");
            }
            return "redirect:/payments/invoice/" + dto.getReservationId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/payments/checkout/" + dto.getReservationId();
        }
    }

    @GetMapping("/payments/invoice/{reservationId}")
    public String viewInvoice(@PathVariable("reservationId") Integer reservationId, Model model) {
        Invoice invoice = paymentService.getInvoiceByReservation(reservationId);
        Reservation reservation = reservationService.getReservationById(reservationId);
        List<Payment> payments = paymentService.getPaymentsByReservation(reservationId);

        model.addAttribute("invoice", invoice);
        model.addAttribute("reservation", reservation);
        model.addAttribute("payments", payments);
        return "payments/invoice";
    }

    @GetMapping("/payments/history")
    public String guestPaymentHistory(HttpSession session, Model model) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null || !currentUser.isGuest()) return "redirect:/login";

        List<Payment> payments = paymentService.getPaymentsByGuest(currentUser.getId());
        model.addAttribute("payments", payments);
        return "payments/history";
    }

    // Staff Payments & Verification
    @GetMapping("/staff/payments")
    public String staffPayments(HttpSession session, Model model) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isStaff()) return "redirect:/dashboard?error=Access Denied: Staff role required";

        List<Payment> allPayments = paymentService.getAllPayments();
        List<Payment> pendingReceipts = paymentService.getPendingVerifications();

        model.addAttribute("payments", allPayments);
        model.addAttribute("pendingReceipts", pendingReceipts);
        return "payments/manage";
    }

    @PostMapping("/staff/payments/verify/{paymentId}")
    public String verifyPaymentReceipt(@PathVariable("paymentId") Integer paymentId,
                                       @RequestParam("approve") boolean approve,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isStaff()) return "redirect:/dashboard?error=Access Denied: Staff role required";

        try {
            paymentService.verifyBankTransferReceipt(paymentId, approve, currentUser.getId());
            if (approve) {
                redirectAttributes.addFlashAttribute("successMessage", "Receipt approved and balance updated!");
            } else {
                redirectAttributes.addFlashAttribute("infoMessage", "Receipt marked as failed.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/staff/payments";
    }

    @PostMapping("/staff/payments/delete/{paymentId}")
    public String deletePayment(@PathVariable("paymentId") Integer paymentId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isIt() && !currentUser.isManager() && !currentUser.isFinance()) {
            return "redirect:/dashboard?error=Access Denied: Finance or Admin role required";
        }

        try {
            paymentService.deletePayment(paymentId, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Payment #" + paymentId + " deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete payment: " + e.getMessage());
        }
        return "redirect:/staff/payments";
    }

    @PostMapping("/staff/payments/update-method/{paymentId}")
    public String updatePaymentMethod(@PathVariable("paymentId") Integer paymentId,
                                      @RequestParam("method") String method,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isStaff()) return "redirect:/dashboard?error=Access Denied: Staff role required";

        try {
            paymentService.updatePaymentMethod(paymentId, method, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Payment method updated to " + method);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/staff/payments";
    }

    @GetMapping("/staff/invoices")
    public String staffInvoices(HttpSession session, Model model) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isStaff()) return "redirect:/dashboard?error=Access Denied: Staff role required";

        model.addAttribute("invoices", paymentService.getAllInvoices());
        return "payments/manage";
    }

    @PostMapping("/staff/invoices/update/{invoiceId}")
    public String updateInvoice(@PathVariable("invoiceId") Integer invoiceId,
                                @RequestParam("totalAmount") java.math.BigDecimal totalAmount,
                                @RequestParam("outstandingBalance") java.math.BigDecimal outstandingBalance,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isFinance() && !currentUser.isManager()) {
            return "redirect:/dashboard?error=Access Denied: Finance role required";
        }

        try {
            paymentService.updateInvoice(invoiceId, totalAmount, outstandingBalance, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Invoice #" + invoiceId + " updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/staff/payments";
    }

    @PostMapping("/staff/invoices/delete/{invoiceId}")
    public String deleteInvoice(@PathVariable("invoiceId") Integer invoiceId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        UserSession currentUser = (UserSession) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        if (currentUser == null) return "redirect:/login";
        if (!currentUser.isIt() && !currentUser.isManager()) {
            return "redirect:/dashboard?error=Access Denied: Admin role required";
        }

        try {
            paymentService.deleteInvoice(invoiceId, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Invoice #" + invoiceId + " deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete invoice: " + e.getMessage());
        }
        return "redirect:/staff/payments";
    }
}
