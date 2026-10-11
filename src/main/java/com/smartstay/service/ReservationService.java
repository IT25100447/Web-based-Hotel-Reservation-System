package com.smartstay.service;

import com.smartstay.dto.ReservationRequestDto;
import com.smartstay.exception.BookingConflictException;
import com.smartstay.exception.ResourceNotFoundException;
import com.smartstay.exception.ValidationException;
import com.smartstay.model.*;
import com.smartstay.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final SpecialRequestRepository specialRequestRepository;
    private final FeedbackRepository feedbackRepository;
    private final RefundRepository refundRepository;
    private final ActivityLogService activityLogService;

    public ReservationService(ReservationRepository reservationRepository,
                              RoomRepository roomRepository,
                              GuestRepository guestRepository,
                              InvoiceRepository invoiceRepository,
                              PaymentRepository paymentRepository,
                              SpecialRequestRepository specialRequestRepository,
                              FeedbackRepository feedbackRepository,
                              RefundRepository refundRepository,
                              ActivityLogService activityLogService) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.specialRequestRepository = specialRequestRepository;
        this.feedbackRepository = feedbackRepository;
        this.refundRepository = refundRepository;
        this.activityLogService = activityLogService;
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> getReservationsByGuest(Integer guestId) {
        return reservationRepository.findByGuestId(guestId);
    }

    public Reservation getReservationById(Integer reservationId) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with ID: " + reservationId));
        populateReservationDetails(res);
        return res;
    }

    @Transactional
    public Reservation createReservation(ReservationRequestDto dto, Integer sessionUserId, boolean isStaff) {
        LocalDate checkIn = dto.getCheckInDate();
        LocalDate checkOut = dto.getCheckOutDate();

        if (checkIn == null || checkOut == null) {
            // Make sure both check-in and check-out dates are provided
            throw new ValidationException("Check-in and check-out dates are required.");
        }
        if (!checkOut.isAfter(checkIn)) {
            // Ensure the guest leaves after they arrive, not before or on the same day
            throw new ValidationException("Check-out date must be strictly after check-in date.");
        }

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Selected room not found."));

        // Double-booking check
        boolean available = roomRepository.isRoomAvailable(room.getRoomId(), checkIn, checkOut, null);
        if (!available) {
            // Stop the booking if the room is already taken for these dates
            throw new BookingConflictException("Room " + room.getRoomNumber() + " is already booked for the selected dates. Please choose another date or room.");
        }

        // Determine Guest
        Integer guestId = dto.getGuestId();
        if (!isStaff) {
            guestId = sessionUserId;
        } else {
            // Staff booking for guest
            if (guestId == null) {
                if (dto.getGuestEmail() != null && !dto.getGuestEmail().isBlank()) {
                    Optional<Guest> existing = guestRepository.findByEmail(dto.getGuestEmail().trim());
                    if (existing.isPresent()) {
                        guestId = existing.get().getGuestId();
                    } else {
                        Guest newGuest = new Guest();
                        newGuest.setName(dto.getGuestName() != null ? dto.getGuestName() : "Walk-in Guest");
                        newGuest.setEmail(dto.getGuestEmail().trim());
                        newGuest.setPhone(dto.getGuestPhone());
                        newGuest.setPassword("$2a$10$wN9a56qP1vDqfJ.M2gUuO.qXlIcfD4C4pC70jRkE7jJm19L27/7re"); // Default test password
                        guestId = guestRepository.save(newGuest);
                    }
                } else {
                    // Force staff to provide the guest's email when making a booking for them
                    throw new ValidationException("Guest information (email) is required for staff booking.");
                }
            }
        }

        Reservation res = new Reservation();
        res.setGuestId(guestId);
        res.setRoomId(room.getRoomId());
        res.setCheckInDate(checkIn);
        res.setCheckOutDate(checkOut);
        res.setNumGuests(dto.getNumGuests() != null ? dto.getNumGuests() : 1);
        res.setBookingDate(LocalDateTime.now());
        res.setStatus("CONFIRMED");

        if (isStaff) {
            res.setEnteredByStaffId(sessionUserId);
        }

        int reservationId = reservationRepository.save(res);
        res.setReservationId(reservationId);

        // Calculate Pricing and Invoice
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) nights = 1;
        BigDecimal baseTotal = room.getPrice().multiply(BigDecimal.valueOf(nights));

        BigDecimal discountPct = roomRepository.getActiveDiscountForRoom(room.getRoomId());
        BigDecimal totalAmount = baseTotal;
        if (discountPct != null && discountPct.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = baseTotal.multiply(discountPct).divide(BigDecimal.valueOf(100));
            totalAmount = baseTotal.subtract(discount);
        }

        Invoice invoice = new Invoice();
        invoice.setReservationId(reservationId);
        invoice.setTotalAmount(totalAmount);
        invoice.setOutstandingBalance(totalAmount);
        invoice.setIssueDate(LocalDateTime.now());
        invoiceRepository.save(invoice);

        // Special Request
        if (dto.getSpecialRequestType() != null && !dto.getSpecialRequestType().isBlank()) {
            SpecialRequest sr = new SpecialRequest();
            sr.setReservationId(reservationId);
            sr.setType(dto.getSpecialRequestType());
            sr.setDescription(dto.getSpecialRequestDescription());
            specialRequestRepository.save(sr);
        }

        // Initial Payment / Deposit
        if (dto.getDepositAmount() != null && dto.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
            Payment payment = new Payment();
            payment.setReservationId(reservationId);
            payment.setAmount(dto.getDepositAmount());
            payment.setMethod(dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "ONLINE");
            payment.setReferenceNumber(dto.getPaymentReference() != null ? dto.getPaymentReference() : "DEP-" + System.currentTimeMillis());
            payment.setPaymentDate(LocalDateTime.now());

            if (isStaff) {
                payment.setProcessedByStaffId(sessionUserId);
                payment.setStatus("VERIFIED");
            } else {
                if ("ONLINE".equalsIgnoreCase(dto.getPaymentMethod()) || "CARD".equalsIgnoreCase(dto.getPaymentMethod())) {
                    payment.setStatus("VERIFIED"); // Simulated instant online/card payment
                } else {
                    payment.setStatus("PENDING"); // Bank transfer requires staff verification
                }
            }

            paymentRepository.save(payment);
            if ("VERIFIED".equalsIgnoreCase(payment.getStatus())) {
                invoiceRepository.reduceOutstandingBalance(reservationId, dto.getDepositAmount());
            }
        }

        if (isStaff) {
            activityLogService.log(sessionUserId, "Created reservation #" + reservationId + " for Room #" + room.getRoomNumber());
        }

        return getReservationById(reservationId);
    }

    @Transactional
    public void modifyReservation(Integer reservationId, LocalDate newCheckIn, LocalDate newCheckOut, int numGuests, Integer staffId) {
        Reservation res = getReservationById(reservationId);
        if ("CANCELLED".equalsIgnoreCase(res.getStatus()) || "CHECKED_OUT".equalsIgnoreCase(res.getStatus())) {
            // Prevent changing bookings that are already finished or cancelled
            throw new ValidationException("Cannot modify a cancelled or checked-out reservation.");
        }

        if (!newCheckOut.isAfter(newCheckIn)) {
            // Ensure the new check-out date is correct
            throw new ValidationException("Check-out date must be after check-in date.");
        }

        boolean available = roomRepository.isRoomAvailable(res.getRoomId(), newCheckIn, newCheckOut, reservationId);
        if (!available) {
            // Check if the room is free for the new dates
            throw new BookingConflictException("Room is not available for the newly requested dates.");
        }

        reservationRepository.updateDatesAndGuests(reservationId, java.sql.Date.valueOf(newCheckIn), java.sql.Date.valueOf(newCheckOut), numGuests);

        // Recalculate invoice
        long nights = ChronoUnit.DAYS.between(newCheckIn, newCheckOut);
        if (nights <= 0) nights = 1;
        BigDecimal baseTotal = res.getRoomPrice().multiply(BigDecimal.valueOf(nights));
        BigDecimal discountPct = roomRepository.getActiveDiscountForRoom(res.getRoomId());
        BigDecimal newTotal = baseTotal;
        if (discountPct != null && discountPct.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = baseTotal.multiply(discountPct).divide(BigDecimal.valueOf(100));
            newTotal = baseTotal.subtract(discount);
        }

        // Compute new balance
        List<Payment> payments = paymentRepository.findByReservationId(reservationId);
        BigDecimal totalPaid = payments.stream()
                .filter(p -> "VERIFIED".equalsIgnoreCase(p.getStatus()))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal newBalance = newTotal.subtract(totalPaid);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            newBalance = BigDecimal.ZERO;
        }

        invoiceRepository.updateTotalAndBalance(reservationId, newTotal, newBalance);
        activityLogService.log(staffId, "Modified dates for reservation #" + reservationId);
    }

    @Transactional
    public void cancelReservation(Integer reservationId, String reason, Integer sessionUserId, boolean isStaff) {
        Reservation res = getReservationById(reservationId);
        if ("CANCELLED".equalsIgnoreCase(res.getStatus())) {
            // Stop the user from cancelling a booking that is already cancelled
            throw new ValidationException("Reservation is already cancelled.");
        }

        reservationRepository.updateStatus(reservationId, "CANCELLED");

        // Release room if assigned
        if (res.getRoomId() != null) {
            try {
                roomRepository.updateStatus(res.getRoomId(), "AVAILABLE");
            } catch (Exception ignored) {}
        }

        // If guest had paid amount, create Feedback ticket + Refund record in PENDING status
        List<Payment> payments = paymentRepository.findByReservationId(reservationId);
        BigDecimal totalPaid = payments.stream()
                .filter(p -> "VERIFIED".equalsIgnoreCase(p.getStatus()))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            try {
                Feedback feedback = new Feedback();
                feedback.setGuestId(res.getGuestId());
                feedback.setReservationId(reservationId);
                feedback.setCategory("CANCELLATION");
                feedback.setRating(3);
                feedback.setComments("Cancellation refund request. Reason: " + (reason != null && !reason.isBlank() ? reason : "Guest requested cancellation"));
                feedback.setStatus("OPEN");
                feedback.setSubmittedDate(LocalDateTime.now());
                int feedbackId = feedbackRepository.save(feedback);

                if (feedbackId > 0) {
                    Refund refund = new Refund();
                    refund.setFeedbackId(feedbackId);
                    refund.setAmount(totalPaid);
                    refund.setStatus("PENDING");
                    refund.setReason(reason != null && !reason.isBlank() ? reason : "Refund for cancelled booking #" + reservationId);
                    refundRepository.save(refund);
                }
            } catch (Exception e) {
                // Keep cancellation successful even if automated refund ticket logging encounters an issue
            }
        }

        if (isStaff && sessionUserId != null) {
            activityLogService.log(sessionUserId, "Cancelled reservation #" + reservationId);
        }
    }

    @Transactional
    public void checkIn(Integer reservationId, Integer staffId) {
        Reservation res = getReservationById(reservationId);
        reservationRepository.updateStatus(reservationId, "CHECKED_IN");
        roomRepository.updateStatus(res.getRoomId(), "OCCUPIED");
        activityLogService.log(staffId, "Checked-in guest for reservation #" + reservationId);
    }

    @Transactional
    public void checkOut(Integer reservationId, Integer staffId) {
        Reservation res = getReservationById(reservationId);
        reservationRepository.updateStatus(reservationId, "CHECKED_OUT");
        roomRepository.updateStatus(res.getRoomId(), "MAINTENANCE"); // Set to maintenance/cleaning readiness
        activityLogService.log(staffId, "Checked-out guest for reservation #" + reservationId + ". Room marked for cleaning/maintenance.");
    }

    @Transactional
    public void deleteReservation(Integer reservationId, Integer staffId) {
        Reservation res = getReservationById(reservationId);
        reservationRepository.delete(reservationId);
        activityLogService.log(staffId, "Hard-deleted reservation #" + reservationId + " (Guest: " + res.getGuestName() + ")");
    }

    @Transactional
    public void removeSpecialRequest(Integer requestId, Integer staffId) {
        specialRequestRepository.delete(requestId);
        activityLogService.log(staffId, "Removed special request #" + requestId);
    }

    private void populateReservationDetails(Reservation res) {
        res.setSpecialRequests(specialRequestRepository.findByReservationId(res.getReservationId()));
        invoiceRepository.findByReservationId(res.getReservationId()).ifPresent(res::setInvoice);
        res.setPayments(paymentRepository.findByReservationId(res.getReservationId()));
    }
}
