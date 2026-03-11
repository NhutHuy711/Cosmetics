package com.cosmetics.shoppingcart;

import java.util.List;

import com.cosmetics.common.entity.product.ProductVariant;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.cosmetics.common.entity.CartItem;
import com.cosmetics.common.entity.Customer;

@Repository
public interface CartItemRepository extends CrudRepository<CartItem, Integer> {

    @Query("SELECT c FROM CartItem c WHERE c.customer = :customer AND c.variant.enabled = true " +
            "AND c.variant.product.category.enabled = true AND c.variant.product.brand.enabled = true")
    List<CartItem> findByCustomer(Customer customer);

    CartItem findByCustomerAndVariant(Customer customer, ProductVariant productVariant);

    @Modifying
    @Query("UPDATE CartItem c SET c.quantity = ?1 WHERE c.customer.id = ?2 AND c.variant.id = ?3")
    void updateQuantity(Integer quantity, Integer customerId, Integer productVariantId);

    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.customer.id = ?1 AND c.variant.id = ?2")
    void deleteByCustomerAndProductVariant(Integer customerId, Integer productVariantId);

    @Modifying
    @Query("DELETE CartItem c WHERE c.customer.id = ?1")
    void deleteByCustomer(Integer customerId);

    @Query("SELECT c FROM CartItem c WHERE c.customer.id = ?1 AND c.variant.id = ?2")
    CartItem findByCustomerAndVariant(Integer customerId, Integer productVariantId);
}
