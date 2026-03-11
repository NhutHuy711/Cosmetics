package com.cosmetics.common.entity;

import com.cosmetics.common.entity.product.ProductVariant;

import javax.persistence.*;

@Entity
@Table(name = "cart_items")
public class CartItem extends IdBasedEntity {

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    private int quantity;

    @Transient
    private float shippingCost;

    public CartItem() {
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public ProductVariant getVariant() {
        return variant;
    }

    public void setVariant(ProductVariant variant) {
        this.variant = variant;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "CartItem [id=" + id + ", customer=" + (customer != null ? customer.getFullName() : "") +
                ", variant=" + (variant != null && variant.getProduct() != null ? variant.getProduct().getShortName() : "") +
                ", quantity=" + quantity + "]";
    }

    @Transient
    public float getSubtotal() {
        return variant.getPrice() * quantity;
    }

    @Transient
    public float getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(float shippingCost) {
        this.shippingCost = shippingCost;
    }
}
