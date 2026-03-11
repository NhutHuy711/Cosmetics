package com.cosmetics.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.cosmetics.common.entity.order.OrderDetail;
import com.cosmetics.common.entity.order.OrderStatus;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    @Query("""
    SELECT COUNT(d)
    FROM OrderDetail d
    JOIN d.order o
    WHERE d.variant.id = ?1
      AND o.customer.id = ?2
      AND o.status = ?3
""")
    Long countByVariantAndCustomerAndOrderStatus(Integer variantId, Integer customerId, OrderStatus status);

}
