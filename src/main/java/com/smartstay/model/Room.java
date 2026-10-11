package com.smartstay.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Room {
    private Integer roomId;
    private Integer branchId;
    private String roomNumber;
    private String type;
    private BigDecimal price;
    private String status; // 'AVAILABLE','OCCUPIED','MAINTENANCE'

    // Transient fields for views
    private String branchName;
    private List<RoomImage> images = new ArrayList<>();
    private List<String> facilities = new ArrayList<>();
    private BigDecimal activeDiscountPct = BigDecimal.ZERO;
    private BigDecimal discountedPrice;
    private String primaryImageUrl;

    public Room() {
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public void setBranchId(Integer branchId) {
        this.branchId = branchId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public List<RoomImage> getImages() {
        return images;
    }

    public void setImages(List<RoomImage> images) {
        this.images = images;
    }

    public List<String> getFacilities() {
        return facilities;
    }

    public void setFacilities(List<String> facilities) {
        this.facilities = facilities;
    }

    public BigDecimal getActiveDiscountPct() {
        return activeDiscountPct != null ? activeDiscountPct : BigDecimal.ZERO;
    }

    public void setActiveDiscountPct(BigDecimal activeDiscountPct) {
        this.activeDiscountPct = activeDiscountPct;
    }

    public boolean hasDiscount() {
        return activeDiscountPct != null && activeDiscountPct.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getDiscountedPrice() {
        if (discountedPrice != null) {
            return discountedPrice;
        }
        if (price != null && hasDiscount()) {
            BigDecimal discount = price.multiply(activeDiscountPct).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            return price.subtract(discount);
        }
        return price != null ? price : BigDecimal.ZERO;
    }

    public void setDiscountedPrice(BigDecimal discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public String getPrimaryImageUrl() {
        if (primaryImageUrl != null && !primaryImageUrl.isBlank()) {
            return primaryImageUrl;
        }
        if (images != null && !images.isEmpty()) {
            return images.get(0).getImageUrl();
        }
        return "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=800&q=80";
    }

    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.primaryImageUrl = primaryImageUrl;
    }
}
