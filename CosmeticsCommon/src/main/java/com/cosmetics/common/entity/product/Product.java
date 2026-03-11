package com.cosmetics.common.entity.product;

import com.cosmetics.common.entity.Brand;
import com.cosmetics.common.entity.Category;
import com.cosmetics.common.entity.IdBasedEntity;
import com.cosmetics.common.entity.Review;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

import com.cosmetics.common.entity.storage.ImportDetail;

@Entity
@Table(name = "products")
public class Product extends IdBasedEntity {

    @Column(unique = true, length = 256, nullable = false)
    private String name;

    @Column(unique = true, length = 256, nullable = false)
    private String alias;

    @Column(name = "full_description", length = 4096, nullable = false)
    private String fullDescription;

//    @Column(name = "main_image", length = 255, nullable = false)
//    private String mainImage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean enabled;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductVariant> variants = new HashSet<>();

//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<ProductDetail> details = new ArrayList<>();

    @Transient
    private boolean customerCanReview;

    @Transient
    private boolean reviewedByCustomer;

    public Product(Integer id) {
        this.id = id;
    }

    public Product(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Product() {
    }

    public Product(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

//    public String getMainImage() {
//        return mainImage;
//    }
//
//    public void setMainImage(String mainImage) {
//        this.mainImage = mainImage;
//    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }


    public Set<ProductVariant> getVariants() {
        return variants;
    }

    public void setVariants(Set<ProductVariant> variants) {
        this.variants = variants;
        this.variants.forEach(variant -> variant.setProduct(this));
    }

    private ProductVariant getOrCreateDefaultVariant() {
        if (variants == null) {
            variants = new HashSet<>();
        }
        return variants.stream().findFirst().orElseGet(() -> {
            ProductVariant variant = new ProductVariant();
            variant.setProduct(this);
            variants.add(variant);
            return variant;
        });
    }

    public void addExtraImage(String imageName) {
        ProductVariant variant = getOrCreateDefaultVariant();
        variant.addImage(new ProductImage(imageName, variant));
    }

    public boolean containsImageName(String imageName) {
        return getOrCreateDefaultVariant().containsImageName(imageName);
    }

//    public List<ProductDetail> getDetails() {
//        return details;
//    }
//
//    public void setDetails(List<ProductDetail> details) {
//        this.details = details;
//    }

    @Transient
    public List<ImportDetail> getImportDetails() {
        List<ImportDetail> aggregatedDetails = new ArrayList<>();
        if (variants != null) {
            for (ProductVariant variant : variants) {
                if (variant != null && variant.getImportDetails() != null) {
                    aggregatedDetails.addAll(variant.getImportDetails());
                }
            }
        }
        return aggregatedDetails;
    }

//    public void addDetail(String name, String value) {
//        this.details.add(new ProductDetail(name, value, this));
//    }
//
//    public void addDetail(Integer id, String name, String value) {
//        this.details.add(new ProductDetail(id, name, value, this));
//    }

    @Transient
    public List<Review> getReviews() {
        List<Review> aggregatedReviews = new ArrayList<>();
        if (variants != null) {
            for (ProductVariant variant : variants) {
                if (variant != null && variant.getReviews() != null) {
                    aggregatedReviews.addAll(variant.getReviews());
                }
            }
        }
        return aggregatedReviews;
    }

    public void setReviews(List<Review> reviews) {
        ProductVariant variant = getOrCreateDefaultVariant();
        variant.setReviews(reviews);
        if (reviews != null) {
            reviews.forEach(review -> {
                if (review != null) {
                    review.setProductVariant(variant);
                }
            });
        }
    }

    public boolean isCustomerCanReview() {
        return customerCanReview;
    }

    public void setCustomerCanReview(boolean customerCanReview) {
        this.customerCanReview = customerCanReview;
    }

    public boolean isReviewedByCustomer() {
        return reviewedByCustomer;
    }

    public void setReviewedByCustomer(boolean reviewedByCustomer) {
        this.reviewedByCustomer = reviewedByCustomer;
    }

    public boolean hasReviewByCustomer(Integer customerId) {
        if (reviewedByCustomer) return true;

        List<Review> reviews = getReviews();
        if (reviews == null) return false;

        for (Review review : reviews) {
            if (review.getCustomer() == null) continue;
            if (review.getCustomer().getId().equals(customerId)) {
                reviewedByCustomer = true;
                return true;
            }
        }

        return false;
    }

    @Transient
    public String getShortDescription() {
        if (fullDescription == null) {
            return "";
        }
        return fullDescription.length() <= 200 ? fullDescription : fullDescription.substring(0, 200) + "...";
    }

    @Transient
    public String getShortName() {
        if (name == null) {
            return "";
        }
        return name.length() > 70 ? name.substring(0, 70).concat("...") : name;
    }

//    @Transient
//    public Float getPrice() {
//        return variants.stream()
//                .filter(ProductVariant::isEnabled)
//                .map(ProductVariant::getPrice)
//                .filter(Objects::nonNull)
//                .map(price -> price.floatValue())
//                .min(Float::compare)
//                .orElse(null);
//    }
//
//    public void setPrice(float price) {
//        ProductVariant variant = getOrCreateDefaultVariant();
//        variant.setPrice(java.math.BigDecimal.valueOf(price));
//    }

//    @Transient
//    public float getLength() {
//        return 0f;
//    }
//
//    public void setLength(float ignored) {
//        // Dimensions are not tracked in the current schema
//    }
//
//    @Transient
//    public float getWidth() {
//        return 0f;
//    }
//
//    public void setWidth(float ignored) {
//        // Dimensions are not tracked in the current schema
//    }
//
//    @Transient
//    public float getHeight() {
//        return 0f;
//    }
//
//    public void setHeight(float ignored) {
//        // Dimensions are not tracked in the current schema
//    }
//
//    @Transient
//    public int getInStock() {
//        if (variants == null || variants.isEmpty()) {
//            return 0;
//        }
//
//        return variants.stream()
//                .filter(Objects::nonNull)
//                .filter(ProductVariant::isEnabled)
//                .map(ProductVariant::getStock)
//                .filter(Objects::nonNull)
//                .mapToInt(Integer::intValue)
//                .sum();
//    }
//
//    public void setInStock(int stock) {
//        ProductVariant variant = getOrCreateDefaultVariant();
//        variant.setStock(stock);
//    }

//    @Transient
//    public String getMainImagePath() {
//        if (id == null || mainImage == null) return "/images/image-thumbnail.png";
//
//        return Constants.S3_BASE_URI + "/product-images/" + this.id + "/" + this.mainImage;
//    }

    @Transient
    public String getURI() {
        return "/p/" + this.alias + "/";
    }

    @Override
    public String toString() {
        return "Product [id=" + id + ", name=" + name + "]";
    }
}
