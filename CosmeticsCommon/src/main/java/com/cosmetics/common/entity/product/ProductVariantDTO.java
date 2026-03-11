package com.cosmetics.common.entity.product;

public class ProductVariantDTO {
    private Integer id;
    private String name;
    private String mainImagePath;
    private String productName;

    public ProductVariantDTO(ProductVariant variant) {
        this.id = variant.getId();
        this.name = variant.getName();
        this.mainImagePath = variant.getMainImagePath();
        this.productName = variant.getProduct() != null ? variant.getProduct().getName() : null;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMainImagePath() {
        return mainImagePath;
    }

    public void setMainImagePath(String mainImagePath) {
        this.mainImagePath = mainImagePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}

