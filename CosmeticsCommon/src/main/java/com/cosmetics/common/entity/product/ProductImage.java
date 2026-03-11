package com.cosmetics.common.entity.product;

import com.cosmetics.common.Constants;
import com.cosmetics.common.entity.IdBasedEntity;

import javax.persistence.*;

@Entity
@Table(name = "product_images")
public class ProductImage extends IdBasedEntity {

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    public ProductImage() {
    }

    public ProductImage(Integer id, String name, ProductVariant productVariant) {
        this.id = id;
        this.name = name;
        this.productVariant = productVariant;
    }

    public ProductImage(String name, ProductVariant productVariant) {
        this.name = name;
        this.productVariant = productVariant;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProductVariant getProductVariant() {
        return productVariant;
    }

    public void setProductVariant(ProductVariant productVariant) {
        this.productVariant = productVariant;
    }

    @Transient
    public String getImagePath() {
        if (productVariant == null || productVariant.getProduct() == null) {
            return "/images/image-thumbnail.png";
        }
        return Constants.S3_BASE_URI + "/product-images/" + productVariant.getProduct().getId() + "/extras/" + this.name;
    }
}
