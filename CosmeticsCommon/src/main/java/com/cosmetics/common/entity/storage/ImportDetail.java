package com.cosmetics.common.entity.storage;

import com.cosmetics.common.entity.IdBasedEntity;
import com.cosmetics.common.entity.product.ProductVariant;

import javax.persistence.*;

@Entity
@Table(name = "import_details")
public class ImportDetail extends IdBasedEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_id", nullable = false)
    private Import importField;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "cost", nullable = false)
    private Float cost;

    public Import getImportField() {
        return importField;
    }

    public void setImportField(Import importField) {
        this.importField = importField;
    }

    public ProductVariant getProductVariant() {
        return productVariant;
    }

    public void setProductVariant(ProductVariant productVariant) {
        this.productVariant = productVariant;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Float getCost() {
        return cost;
    }

    public void setCost(Float cost) {
        this.cost = cost;
    }

    public ImportDetail() {
    }

    public ImportDetail(ProductVariant productVariant, Integer quantity, Float cost, Import ip) {
        super();
        this.productVariant = productVariant;
        this.quantity = quantity;
        this.cost = cost;
        this.importField = ip;
    }

    @Override
    public String toString() {
        return "ImportDetail{" +
                "id=" + importField.getId() +
                ", productVariant=" + productVariant +
                ", quantity=" + quantity +
                ", cost=" + cost + "}";
    }
}
