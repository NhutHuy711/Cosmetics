package com.cosmetics.common.entity.promotion;

import com.cosmetics.common.entity.IdBasedEntity;
import com.cosmetics.common.entity.product.ProductVariant;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "promotion_products", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pp_promo_variant", columnNames = {"promotion_id", "variant_id"})
})
public class PromotionProduct extends IdBasedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(name = "percent_off", precision = 5, scale = 2, nullable = false)
    private BigDecimal percentOff;

    @Transient
    private Integer variantId;

    public Integer getVariantId() {
        return variantId != null ? variantId :
                (variant != null ? variant.getId() : null);
    }

    public void setVariantId(Integer variantId) {
        this.variantId = variantId;
        if (variant == null) {
            variant = new ProductVariant();
        }
        variant.setId(variantId);
    }


    public Promotion getPromotion() {
        return promotion;
    }

    public void setPromotion(Promotion promotion) {
        this.promotion = promotion;
    }

    public ProductVariant getVariant() {
        return variant;
    }

    public void setVariant(ProductVariant variant) {
        this.variant = variant;
    }

    public BigDecimal getPercentOff() {
        return percentOff;
    }

    public void setPercentOff(BigDecimal percentOff) {
        this.percentOff = percentOff;
    }
}
