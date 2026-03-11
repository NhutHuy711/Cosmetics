package com.cosmetics.common.entity.order;

import com.cosmetics.common.entity.IdBasedEntity;
import com.cosmetics.common.entity.product.ProductVariant;
import org.hibernate.annotations.Formula;

import javax.persistence.*;

@Entity
@Table(name = "order_details")
public class OrderDetail extends IdBasedEntity {
    @Column(nullable = false)
    private int quantity;
    @Column(name = "product_cost", nullable = false, precision = 12, scale = 2)
    private float productCost;
    @Column(name = "shipping_cost", nullable = false, precision = 12, scale = 2)
    private float shippingCost;
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private float unitPrice;

    @ManyToOne
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Formula("(COALESCE(quantity,0) * COALESCE(unit_price,0) + COALESCE(shipping_cost,0))")
    private float subtotal;

    public OrderDetail() {
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getProductCost() {
        return productCost;
    }

    public void setProductCost(float productCost) {
        this.productCost = productCost;
    }

    public float getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(float shippingCost) {
        this.shippingCost = shippingCost;
    }

    public float getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(float unitPrice) {
        this.unitPrice = unitPrice;
    }

    public float getSubtotal() {
        return subtotal;
    }

    public ProductVariant getVariant() {
        return variant;
    }

    public void setVariant(ProductVariant variant) {
        this.variant = variant;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
