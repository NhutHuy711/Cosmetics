package com.cosmetics.common.entity.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.cosmetics.common.Constants;

import java.util.List;

public class ProductVariantDocument {

    private String id;
    private String productId;
    private String name;
    private String alias;
    private String productName;
    private String brand;
    private String category;
    private String description;

    private float price;
    private float finalPrice;
    private float discountPercent;

    private float averageRating;
    private int reviewCount;

    private String mainImage;

    private int stock; // cần cho button Add to Cart

    public List<OptionKV> options;

    public static class OptionKV {
        private String type;
        private String value;
        private Integer typeId;
        private Integer valueId;

        // getters/setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }

        public Integer getTypeId() { return typeId; }
        public void setTypeId(Integer typeId) { this.typeId = typeId; }

        public Integer getValueId() { return valueId; }
        public void setValueId(Integer valueId) { this.valueId = valueId; }
    }

    // ===================== BASIC GETTER/SETTER =====================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public float getPrice() { return price; }
    public void setPrice(float price) { this.price = price; }

    public float getFinalPrice() { return finalPrice; }
    public void setFinalPrice(float finalPrice) { this.finalPrice = finalPrice; }

    public float getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(float discountPercent) { this.discountPercent = discountPercent; }

    public float getAverageRating() { return averageRating; }
    public void setAverageRating(float rating) { this.averageRating = averageRating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public String getMainImage() { return mainImage; }
    public void setMainImage(String mainImage) { this.mainImage = mainImage; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public List<OptionKV> getOptions() { return options; }
    public void setOptions(List<OptionKV> options) { this.options = options; }

    // ===================== CUSTOM GETTERS (để khớp UI) =====================

    /** Đường dẫn đến trang chi tiết sản phẩm */
    @JsonIgnore
    public String getURI() {
        return "/p/" + alias;
    }

    /** Đường dẫn ảnh chính */
    @JsonIgnore
    public String getMainImagePath() {
        if (mainImage == null || mainImage.isBlank()) {
            return "/images/default-product.png";
        }
        return Constants.S3_BASE_URI + "/product-images/" + id + "/" + mainImage;
    }

    /** Giá sau giảm */
    @JsonIgnore
    public float getDiscountPrice() {
        if (discountPercent <= 0) return price;
        return finalPrice;
    }

    // ============= MOCK NESTED OBJECTS CHO UI THYMELEAF =================

    @JsonIgnore
    public ProductMini getProduct() {
        ProductMini p = new ProductMini();
        p.setCategory(new CategoryMini(category));
        return p;
    }

    // ---- Inner classes ----

    public static class ProductMini {
        private CategoryMini category;

        public CategoryMini getCategory() { return category; }
        public void setCategory(CategoryMini category) { this.category = category; }
    }

    public static class CategoryMini {
        private String name;

        public CategoryMini(String name) { this.name = name; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
