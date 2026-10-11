package com.smartstay.model;

import java.time.LocalDateTime;

public class Feedback {
    private Integer feedbackId;
    private Integer guestId;
    private Integer reservationId;
    private Integer handledByStaffId;
    private Integer rating;
    private String comments;
    private String category;
    private String status; // 'OPEN','IN_PROGRESS','RESOLVED'
    private LocalDateTime submittedDate;

    // Transient fields
    private String guestName;
    private String guestEmail;
    private String handledByStaffName;
    private String roomNumber;
    private Refund refund;

    public Feedback() {
    }

    public Integer getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(Integer feedbackId) {
        this.feedbackId = feedbackId;
    }

    public Integer getGuestId() {
        return guestId;
    }

    public void setGuestId(Integer guestId) {
        this.guestId = guestId;
    }

    public Integer getReservationId() {
        return reservationId;
    }

    public void setReservationId(Integer reservationId) {
        this.reservationId = reservationId;
    }

    public Integer getHandledByStaffId() {
        return handledByStaffId;
    }

    public void setHandledByStaffId(Integer handledByStaffId) {
        this.handledByStaffId = handledByStaffId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(LocalDateTime submittedDate) {
        this.submittedDate = submittedDate;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }

    public String getHandledByStaffName() {
        return handledByStaffName;
    }

    public void setHandledByStaffName(String handledByStaffName) {
        this.handledByStaffName = handledByStaffName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Refund getRefund() {
        return refund;
    }

    public void setRefund(Refund refund) {
        this.refund = refund;
    }
}
