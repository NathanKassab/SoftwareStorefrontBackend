package me.bannock.capstone.backend.products.service.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class ProductModel {

    public ProductModel(){}

    protected ProductModel(long ownerUid, String productName,
                           String productDescription, String productIconUrl,
                           String productPurchaseUrl, String keygenId,
                           double price){
        this.name = productName;
        this.description = productDescription;
        this.ownerUid = ownerUid;
        this.iconUrl = productIconUrl;
        this.purchaseUrl = productPurchaseUrl;
        this.keygenId = keygenId;
        this.disabled = false;
        this.approved = false;
        this.hidden = true;
        this.price = price;
    }

    @Id
    @Column(name = "id", unique = true)
    @SequenceGenerator(name = "product_seq", sequenceName = "PRODUCT_SEQ", initialValue = 0, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "product_seq")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "owner_uid")
    private Long ownerUid;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "purchase_url")
    private String purchaseUrl;

    @Column(name = "keygen_id")
    private String keygenId;

    @Column(name = "disabled")
    private boolean disabled;

    @Column(name = "hidden")
    private boolean hidden;

    @Column(name = "approved")
    private boolean approved;

    @Column(name = "price")
    private double price;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(Long ownerUid) {
        this.ownerUid = ownerUid;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getPurchaseUrl() {
        return purchaseUrl;
    }

    public void setPurchaseUrl(String purchaseUrl) {
        this.purchaseUrl = purchaseUrl;
    }

    public String getKeygenId() {
        return keygenId;
    }

    public void setKeygenId(String keygenId) {
        this.keygenId = keygenId;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "ProductModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", ownerUid=" + ownerUid +
                ", iconUrl='" + iconUrl + '\'' +
                ", purchaseUrl='" + purchaseUrl + '\'' +
                ", keygenId='***'" +
                ", disabled=" + disabled +
                ", hidden=" + hidden +
                ", approved=" + approved +
                ", price=" + price +
                '}';
    }

}
