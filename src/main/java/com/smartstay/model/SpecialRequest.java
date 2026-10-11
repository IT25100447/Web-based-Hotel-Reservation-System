package com.smartstay.model;

public class SpecialRequest {
    private Integer requestId;
    private Integer reservationId;
    private String type; // e.g. 'BEDDING', 'CHECK_IN', 'DIETARY', 'CORPORATE', 'GROUP', 'OTHER'
    private String description;

    public SpecialRequest() {
    }

    public SpecialRequest(Integer requestId, Integer reservationId, String type, String description) {
        this.requestId = requestId;
        this.reservationId = reservationId;
        this.type = type;
        this.description = description;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public Integer getReservationId() {
        return reservationId;
    }

    public void setReservationId(Integer reservationId) {
        this.reservationId = reservationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
