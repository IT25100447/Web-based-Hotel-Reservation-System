package com.smartstay.model;

public class RoomImage {
    private Integer imageId;
    private Integer roomId;
    private String imageUrl;

    public RoomImage() {
    }

    public RoomImage(Integer imageId, Integer roomId, String imageUrl) {
        this.imageId = imageId;
        this.roomId = roomId;
        this.imageUrl = imageUrl;
    }

    public Integer getImageId() {
        return imageId;
    }

    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
