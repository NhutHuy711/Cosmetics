package com.cosmetics.common.entity.product;

import com.cosmetics.common.Constants;
import com.cosmetics.common.entity.IdBasedEntity;
import com.cosmetics.common.entity.Review;
import com.cosmetics.common.entity.order.OrderDetail;
import com.cosmetics.common.entity.storage.ImportDetail;
import com.cosmetics.common.entity.promotion.PromotionProduct;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "product_variants")
public class ProductVariant extends IdBasedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(precision = 12, scale = 2, nullable = false)
    private float price;

    @Column(precision = 12, scale = 2, nullable = false)
    private float cost = 0f;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(precision = 10, scale = 3, nullable = false)
    private float weight = 0f;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "main_image", length = 255)
    private String mainImage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(unique = true, length = 256, nullable = false)
    private String alias;

    @Column(unique = true, length = 256, nullable = false)
    private String name;

    @ManyToMany
    @JoinTable(
            name = "variant_option_values",
            joinColumns = @JoinColumn(name = "product_variant_id"),
            inverseJoinColumns = @JoinColumn(name = "option_value_id")
    )
    private Set<OptionValue> optionValues = new HashSet<>();

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductImage> images = new HashSet<>();

    @OneToMany(mappedBy = "productVariant")
    private List<ImportDetail> importDetails;

    @OneToMany(mappedBy = "variant")
    private List<OrderDetail> orderDetails;

    @OneToMany(mappedBy = "productVariant")
    private List<Review> reviews;

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PromotionProduct> promotionProducts = new HashSet<>();

    @Formula("(select coalesce(avg(r.rating), 0) " +
            " from reviews r " +
            " where r.product_variant_id = id)")
    private float averageRating;

    @Formula("(select count(r.id) " +
            " from reviews r " +
            " where r.product_variant_id = id)")
    private int reviewCount;



    @Formula("(" +
            "SELECT COALESCE(MAX(pp.percent_off), 0) " +
            "FROM promotion_products pp " +
            "JOIN promotions pr ON pp.promotion_id = pr.id " +
            "WHERE pp.variant_id = id " +
            "  AND pr.enabled = 1 " +
            "  AND NOW() BETWEEN pr.start_at AND pr.end_at" +
            ")")
    private float discountPercent;

    @Formula("(" +
            "price * (1 - (" +
            "COALESCE((" +
            "SELECT MAX(pp.percent_off) " +
            "FROM promotion_products pp " +
            "JOIN promotions pr ON pr.id = pp.promotion_id " +
            "WHERE pp.variant_id = id " +
            "  AND pr.enabled = 1 " +
            "  AND NOW() BETWEEN pr.start_at AND pr.end_at" +
            "), 0) / 100" +
            "))" +
            ")")
    private float finalPrice;


    @Transient
    private boolean customerCanReview;
    @Transient
    private boolean reviewedByCustomer;

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

    public ProductVariant() {
    }

    public ProductVariant(Integer id) {
        this.id = id;
    }

    public ProductVariant(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

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

    public Set<OptionValue> getOptionValues() {
        return optionValues;
    }

    public void setOptionValues(Set<OptionValue> optionValues) {
        this.optionValues = optionValues;
    }

    public void addImage(ProductImage image) {
        images.add(image);
        image.setProductVariant(this);
    }

    public boolean containsImageName(String imageName) {
        return images.stream().anyMatch(image -> imageName.equals(image.getName()));
    }

    public List<ImportDetail> getImportDetails() {
        return importDetails;
    }

    public void setImportDetails(List<ImportDetail> importDetails) {
        this.importDetails = importDetails;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        if (this.reviews != null) {
            this.reviews.forEach(review -> {
                if (review != null) {
                    review.setProductVariant(this);
                }
            });
        }
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<PromotionProduct> getPromotionProducts() {
        return promotionProducts;
    }

    public void setPromotionProducts(Set<PromotionProduct> promotionProducts) {
        this.promotionProducts = promotionProducts;
    }

    public float getDiscountPercent() { return discountPercent; }

    @Transient
    public String getMainImagePath() {
        if (id == null || mainImage == null) return "/images/image-thumbnail.png";

        return Constants.S3_BASE_URI + "/product-images/" + this.id + "/" + this.mainImage;
    }

    public void setImages(Set<ProductImage> images) {
        this.images = images;
    }

    public Set<ProductImage> getImages() {
        return images;
    }

    public void addExtraImage(String imageName) {
        this.images.add(new ProductImage(imageName, this));
    }

    @Transient
    public float getDiscountPrice() {
        float percent = getDiscountPercent();
        return price * (1f - percent / 100f);
    }

    @Transient
    public String getURI() {
        return "/p/" + this.alias + "/";
    }

    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public float getFinalPrice() {
        return finalPrice;
    }

    @Transient
    public String getOptionSummary() {
        if (optionValues == null || optionValues.isEmpty()) return "Default";
        return optionValues.stream()
                .map(ov -> ov.getOptionType().getName() + ": " + ov.getValue())
                .collect(Collectors.joining(", "));
    }

    @Transient
    public Map<String, String> getOptionTypeValueMap() {
        if (optionValues == null || optionValues.isEmpty()) return Collections.emptyMap();

        return optionValues.stream()
                .filter(ov -> ov != null && ov.getOptionType() != null)
                .collect(Collectors.toMap(
                        ov -> ov.getOptionType().getName(),  // key = tên type
                        OptionValue::getValue,               // value = giá trị
                        (a, b) -> a,                         // nếu trùng type, lấy cái đầu
                        LinkedHashMap::new                   // giữ thứ tự insert (tạm)
                ));
    }


}


