package com.smartstay.service;

import com.smartstay.dto.PaymentRequestDto;
import com.smartstay.exception.ResourceNotFoundException;
import com.smartstay.exception.ValidationException;
import com.smartstay.model.Invoice;
import com.smartstay.model.Payment;
import com.smartstay.repository.InvoiceRepository;
import com.smartstay.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final ActivityLogService activityLogService;

    public PaymentService(PaymentRepository paymentRepository,
                          InvoiceRepository invoiceRepository,
                          ActivityLogService activityLogService) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.activityLogService = activityLogService;
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> getPaymentsByReservation(Integer reservationId) {
        return paymentRepository.findByReservationId(reservationId);
    }

    public List<Payment> getPaymentsByGuest(Integer guestId) {
        return paymentRepository.findByGuestId(guestId);
    }

    public List<Payment> getPendingVerifications() {
        return paymentRepository.findPendingVerification();
    }

    public Invoice getInvoiceByReservation(Integer reservationId) {
        return invoiceRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for reservation: " + reservationId));
    }

    public Invoice getInvoiceById(Integer invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));
    }

    @Transactional
    public Payment recordPayment(PaymentRequestDto dto, Integer sessionStaffId, boolean isStaff) {
        Invoice invoice = getInvoiceByReservation(dto.getReservationId());

        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            // Check that the payment is a positive number
            throw new ValidationException("Payment amount must be greater than zero.");
        }

        Payment payment = new Payment();
        payment.setReservationId(dto.getReservationId());
        payment.setMethod(dto.getMethod());
        payment.setAmount(dto.getAmount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setReferenceNumber(dto.getReferenceNumber() != null && !dto.getReferenceNumber().isBlank()
                ? dto.getReferenceNumber()
                : "TXN-" + System.currentTimeMillis());

        if (isStaff) {
            payment.setProcessedByStaffId(sessionStaffId);
            payment.setStatus("VERIFIED");
        } else {
            // Guest checkout
            if ("BANK_TRANSFER".equalsIgnoreCase(dto.getMethod())) {
                payment.setStatus("PENDING"); // Requires staff verification
            } else {
                payment.setStatus("VERIFIED"); // Simulated online / card approval
            }
        }

        paymentRepository.save(payment);

        if ("VERIFIED".equalsIgnoreCase(payment.getStatus())) {
            invoiceRepository.reduceOutstandingBalance(dto.getReservationId(), dto.getAmount());
        }

        if (isStaff) {
            activityLogService.log(sessionStaffId, "Recorded " + dto.getMethod() + " payment of $" + dto.getAmount() + " for reservation #" + dto.getReservationId());
        }

        return payment;
    }

    @Transactional
    public void verifyBankTransferReceipt(Integer paymentId, boolean approve, Integer staffId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found with ID: " + paymentId));

        if (!"PENDING".equalsIgnoreCase(payment.getStatus())) {
            // Only allow verifying payments that are actually waiting to be verified
            throw new ValidationException("Payment is not pending verification.");
        }

        if (approve) {
            paymentRepository.updateStatus(paymentId, "VERIFIED", staffId);
            invoiceRepository.reduceOutstandingBalance(payment.getReservationId(), payment.getAmount());
            activityLogService.log(staffId, "Verified bank transfer payment #" + paymentId + " of $" + payment.getAmount());
        } else {
            paymentRepository.updateStatus(paymentId, "FAILED", staffId);
            activityLogService.log(staffId, "Rejected bank transfer payment #" + paymentId);
        }
    }

    @Transactional
    public void deletePayment(Integer paymentId, Integer staffId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found with ID: " + paymentId));
        paymentRepository.delete(paymentId);
        activityLogService.log(staffId, "Deleted payment #" + paymentId + " (Ref: " + payment.getReferenceNumber() + ")");
    }

    @Transactional
    public void deleteInvoice(Integer invoiceId, Integer staffId) {
        Invoice invoice = getInvoiceById(invoiceId);
        invoiceRepository.delete(invoiceId);
        activityLogService.log(staffId, "Deleted invoice #" + invoiceId + " for reservation #" + invoice.getReservationId());
    }

    @Transactional
    public void updateInvoice(Integer invoiceId, java.math.BigDecimal totalAmount, java.math.BigDecimal outstandingBalance, Integer staffId) {
        invoiceRepository.update(invoiceId, totalAmount, outstandingBalance);
        activityLogService.log(staffId, "Updated invoice #" + invoiceId);
    }

    @Transactional
    public void updatePaymentMethod(Integer paymentId, String method, Integer staffId) {
        paymentRepository.updateMethod(paymentId, method);
        activityLogService.log(staffId, "Updated payment method for payment #" + paymentId + " to " + method);
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }
}
