package me.bannock.capstone.backend.products.service;

public class ProductDTO {

    public ProductDTO(long id, String name, String iconUrl,
                      String purchaseUrl, String description,
                      String keygenId, long ownerUid,
                      boolean disabled, boolean hidden,
                      boolean approved, double price){
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.purchaseUrl = purchaseUrl;
        this.description = description;
        this.keygenId = keygenId;
        this.ownerUid = ownerUid;
        this.disabled = disabled;
        this.hidden = hidden;
        this.approved = approved;
        this.price = price;
    }

    private final long id;
    private String name, iconUrl, purchaseUrl, description, keygenId;
    private Long ownerUid;
    private boolean disabled, hidden, approved;
    private double price;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKeygenId() {
        return keygenId;
    }

    public void setKeygenId(String keygenId) {
        this.keygenId = keygenId;
    }

    public Long getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(Long ownerUid) {
        this.ownerUid = ownerUid;
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
        return "ProductDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", purchaseUrl='" + purchaseUrl + '\'' +
                ", description='" + description + '\'' +
                ", keygenId='" + keygenId + '\'' +
                ", ownerUid=" + ownerUid +
                ", disabled=" + disabled +
                ", hidden=" + hidden +
                ", approved=" + approved +
                ", price=" + price +
                '}';
    }

}
