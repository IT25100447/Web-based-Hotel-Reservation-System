package com.smartstay.model;

public class RoomFacility {
    private Integer facilityId;
    private Integer roomId;
    private String facilityName;

    public RoomFacility() {
    }

    public RoomFacility(Integer facilityId, Integer roomId, String facilityName) {
        this.facilityId = facilityId;
        this.roomId = roomId;
        this.facilityName = facilityName;
    }

    public Integer getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Integer facilityId) {
        this.facilityId = facilityId;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }
}
