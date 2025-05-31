package com.example.gilbertextra.entity;


import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Listing {

    public enum Status {
        PENDING, APPROVED, SOLD, CANCELLED, REMOVED
    }

    private Long listingId;
    private Long sellerId;
    private Long categoryId;
    private Long sizeId; // optional
    private String itemType;
    private String model;
    private String brand;
    private String description;
    private String conditions;
    private String materials;
    private BigDecimal price;
    private BigDecimal maxDiscountPercent; // optional
    private LocalDateTime createdAt;
    private Status status;
    private boolean isFairTrade;
    private boolean isValidated;
    private String color;
    private String sizeLabel;
    private String imageFileName;
    //ikke i db, sted at gemme username til vis med relevant listing
    private String sellerUsername;

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }
    public Long getSellerId() {
        return sellerId;
    }
    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }
    public Long getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    public Long getSizeId() {
        return sizeId;
    }
    public void setSizeId(Long sizeId) {
        this.sizeId = sizeId;
    }
    public String getItemType() {
        return itemType;
    }
    public void setItemType(String itemType) {
        this.itemType = itemType;
    }
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public String getBrand() {
        return brand;
    }
    public void setBrand(String brand) {
        this.brand = brand;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getConditions() {
        return conditions;
    }
    public void setConditions(String conditions) {
        this.conditions = conditions;
    }
    public String getMaterials() {
        return materials;
    }
    public void setMaterials(String materials) {
        this.materials = materials;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public BigDecimal getMaxDiscountPercent() {
        return maxDiscountPercent;
    }
    public void setMaxDiscountPercent(BigDecimal maxDiscountPercent) {
        this.maxDiscountPercent = maxDiscountPercent;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isFairTrade() {
        return isFairTrade;
    }
    public void setFairTrade(boolean fairTrade) {
        isFairTrade = fairTrade;
    }
    public boolean isValidated() {
        return isValidated;
    }
    public void setValidated(boolean validated) {
        isValidated = validated;
    }

    public String getSizeLabel() {
        return sizeLabel;
    }
    public void setSizeLabel(String sizeLabel) {
        this.sizeLabel = sizeLabel;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getImageFileName() {
        return imageFileName;
    }
    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public void setSellerUsername(String sellerUsername) {
        this.sellerUsername = sellerUsername;
    }
}
