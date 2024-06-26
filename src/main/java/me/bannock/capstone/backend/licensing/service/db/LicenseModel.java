package me.bannock.capstone.backend.licensing.service.db;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "licenses")
public class LicenseModel {

    public LicenseModel(){}

    public LicenseModel(long productId, String license){
        Objects.requireNonNull(license);
        this.license = license;
        this.productId = productId;
        this.banned = false;
    }

    @Id
    @Column(name = "id", unique = true)
    @SequenceGenerator(name = "license_seq", sequenceName = "LICENSE_SEQ", initialValue = 0, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "license_seq")
    private Long id;

    @Column(name = "license")
    private String license;

    @Column(name = "product_id")
    private long productId;

    @Column(name = "holder")
    private Long holder;

    @Column(name = "banned")
    private boolean banned;

    public Long getId() {
        return id;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public Long getHolder() {
        return holder;
    }

    public void setHolder(@Nullable Long holder) {
        this.holder = holder;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    @Override
    public String toString() {
        return "LicenseModel{" +
                "id=" + id +
                ", license='" + license + '\'' +
                ", productId=" + productId +
                ", holder=" + holder +
                ", banned=" + banned +
                '}';
    }

}
