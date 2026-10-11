package com.smartstay.model;

public class Branch {
    private Integer branchId;
    private String name;
    private String address;
    private String contactNumber;

    public Branch() {
    }

    public Branch(Integer branchId, String name, String address, String contactNumber) {
        this.branchId = branchId;
        this.name = name;
        this.address = address;
        this.contactNumber = contactNumber;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public void setBranchId(Integer branchId) {
        this.branchId = branchId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
