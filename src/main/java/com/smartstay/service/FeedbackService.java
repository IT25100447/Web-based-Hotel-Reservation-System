package com.smartstay.service;

import com.smartstay.exception.ResourceNotFoundException;
import com.smartstay.exception.ValidationException;
import com.smartstay.model.Feedback;
import com.smartstay.model.Refund;
import com.smartstay.repository.FeedbackRepository;
import com.smartstay.repository.RefundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final RefundRepository refundRepository;
    private final ActivityLogService activityLogService;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           RefundRepository refundRepository,
                           ActivityLogService activityLogService) {
        this.feedbackRepository = feedbackRepository;
        this.refundRepository = refundRepository;
        this.activityLogService = activityLogService;
    }

    public List<Feedback> getAllFeedback() {
        List<Feedback> list = feedbackRepository.findAll();
        for (Feedback f : list) {
            refundRepository.findByFeedbackId(f.getFeedbackId()).ifPresent(f::setRefund);
        }
        return list;
    }

    public List<Feedback> getFeedbackByGuest(Integer guestId) {
        List<Feedback> list = feedbackRepository.findByGuestId(guestId);
        for (Feedback f : list) {
            refundRepository.findByFeedbackId(f.getFeedbackId()).ifPresent(f::setRefund);
        }
        return list;
    }

    public Feedback getFeedbackById(Integer id) {
        Feedback f = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found with ID: " + id));
        refundRepository.findByFeedbackId(f.getFeedbackId()).ifPresent(f::setRefund);
        return f;
    }

    @Transactional
    public Feedback submitFeedback(Integer guestId, Integer reservationId, Integer rating, String category, String comments) {
        Feedback feedback = new Feedback();
        feedback.setGuestId(guestId);
        feedback.setReservationId(reservationId);
        feedback.setRating(rating);
        feedback.setCategory(category != null ? category : "SERVICE");
        feedback.setComments(comments);
        feedback.setStatus("OPEN");
        feedback.setSubmittedDate(LocalDateTime.now());

        int id = feedbackRepository.save(feedback);
        feedback.setFeedbackId(id);
        return feedback;
    }

    @Transactional
    public void updateFeedbackStatus(Integer feedbackId, String status, Integer staffId) {
        feedbackRepository.updateStatus(feedbackId, status, staffId);
        activityLogService.log(staffId, "Updated feedback ticket #" + feedbackId + " status to " + status);
    }

    @Transactional
    public void addStaffResponse(Integer feedbackId, String response, Integer staffId) {
        feedbackRepository.updateResponse(feedbackId, response, staffId);
        activityLogService.log(staffId, "Added staff response to feedback ticket #" + feedbackId);
    }

    @Transactional
    public void editFeedback(Integer feedbackId, Integer rating, String category, String comments, Integer guestId) {
        Feedback f = getFeedbackById(feedbackId);
        if (!f.getGuestId().equals(guestId)) {
            // Ensure a guest can only change the feedback they wrote
            throw new ValidationException("You can only edit your own feedback.");
        }
        feedbackRepository.update(feedbackId, rating, category, comments);
    }

    @Transactional
    public void deleteFeedback(Integer feedbackId, Integer staffId) {
        // Clean up any linked refund records first to prevent foreign key violation
        refundRepository.deleteByFeedbackId(feedbackId);
        feedbackRepository.delete(feedbackId);
        activityLogService.log(staffId, "Deleted feedback ticket #" + feedbackId);
    }

    @Transactional
    public void deleteFeedbackByGuest(Integer feedbackId, Integer guestId) {
        Feedback f = getFeedbackById(feedbackId);
        if (!f.getGuestId().equals(guestId)) {
            // Process validation: Ensure a guest can only delete their own feedback
            throw new ValidationException("You can only delete your own feedback.");
        }
        // Clean up any linked refund records first
        refundRepository.deleteByFeedbackId(feedbackId);
        feedbackRepository.delete(feedbackId);
    }

    public List<Feedback> searchFeedback(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllFeedback();
        }
        return feedbackRepository.searchByKeyword(keyword);
    }

    @Transactional
    public Refund issueRefundForFeedback(Integer feedbackId, BigDecimal amount, String reason, Integer staffId) {
        Feedback f = getFeedbackById(feedbackId);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            // Ensure we are refunding a real positive amount of money
            throw new ValidationException("Refund amount must be greater than zero.");
        }

        Refund refund = new Refund();
        refund.setFeedbackId(feedbackId);
        refund.setAmount(amount);
        refund.setReason(reason);
        refund.setStatus("PENDING");
        refund.setRefundDate(LocalDateTime.now());

        refundRepository.save(refund);
        activityLogService.log(staffId, "Initiated refund request of $" + amount + " for feedback ticket #" + feedbackId);
        return refund;
    }

    @Transactional
    public void processRefundDecision(Integer refundId, String newStatus, Integer staffId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund record not found with ID: " + refundId));

        refundRepository.updateStatus(refundId, newStatus);
        activityLogService.log(staffId, "Updated refund #" + refundId + " status to " + newStatus);
    }

    public List<Refund> getAllRefunds() {
        return refundRepository.findAll();
    }
}
