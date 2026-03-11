package com.cosmetics.common.entity.promotion;

import com.cosmetics.common.entity.IdBasedEntity;
import com.cosmetics.common.entity.product.ProductVariant;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "promotions")
public class Promotion extends IdBasedEntity {

    @Column(nullable = false, length = 128)
    private String name;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private boolean enabled = true;

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PromotionProduct> promotionProducts = new ArrayList<>();

    @Transient
    private BigDecimal percentOff;

    @Transient
    private List<Integer> variantIds = new ArrayList<>();

    public Promotion() {}

    public Promotion(String name, LocalDateTime startAt, LocalDateTime endAt) {
        this.name = name;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public List<PromotionProduct> getPromotionProducts() {
        return promotionProducts;
    }

    public void setPromotionProducts(List<PromotionProduct> promotionProducts) {
        this.promotionProducts = promotionProducts;
        this.variantIds = null;
        this.percentOff = null;
    }

    public BigDecimal getPercentOff() {
        if (percentOff == null) {
            percentOff = promotionProducts.stream()
                    .map(PromotionProduct::getPercentOff)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }
        return percentOff;
    }

    public void setPercentOff(BigDecimal percentOff) {
        this.percentOff = percentOff;
    }

    public List<Integer> getVariantIds() {
        if ((variantIds == null || variantIds.isEmpty()) && promotionProducts != null) {
            LinkedHashSet<Integer> uniqueIds = promotionProducts.stream()
                    .map(PromotionProduct::getVariant)
                    .filter(Objects::nonNull)
                    .map(ProductVariant::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            variantIds = new ArrayList<>(uniqueIds);
        }
        return variantIds;
    }

    public void setVariantIds(List<Integer> variantIds) {
        this.variantIds = variantIds;
    }
}
