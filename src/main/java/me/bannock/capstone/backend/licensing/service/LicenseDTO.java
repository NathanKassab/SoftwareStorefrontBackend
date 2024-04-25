package me.bannock.capstone.backend.licensing.service;

import jakarta.annotation.Nullable;

public class LicenseDTO {

    public LicenseDTO(long id, @Nullable Long holderId, long productId, String license){
        this.id = id;
        this.holderId = id;
        this.productId = productId;
        this.license = license;
    }

    private long id;

    private Long holderId;

    private Long productId;

    private String license;

    public long getId() {
        return id;
    }

    public Long getHolderId() {
        return holderId;
    }

    public void setHolderId(Long holderId) {
        this.holderId = holderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    @Override
    public String toString() {
        return "LicenseDTO{" +
                "id=" + id +
                ", holderId=" + holderId +
                ", productId=" + productId +
                ", license='" + license + '\'' +
                '}';
    }

}
