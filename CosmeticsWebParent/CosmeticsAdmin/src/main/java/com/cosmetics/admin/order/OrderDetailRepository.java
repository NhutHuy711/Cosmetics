package com.cosmetics.admin.order;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.cosmetics.common.entity.order.OrderDetail;
import org.springframework.data.repository.query.Param;

public interface OrderDetailRepository extends CrudRepository<OrderDetail, Integer> {

    @Query("""
        SELECT DISTINCT d
        FROM OrderDetail d
        JOIN FETCH d.variant v
        JOIN FETCH v.product p
        LEFT JOIN FETCH p.category
        WHERE d.order.orderTime BETWEEN :startTime AND :endTime
            AND d.order.status NOT IN (
                com.cosmetics.common.entity.order.OrderStatus.CANCELLED,
                com.cosmetics.common.entity.order.OrderStatus.RETURNED,
                com.cosmetics.common.entity.order.OrderStatus.REFUNDED
            )
            AND (
                (d.order.paymentMethod = com.cosmetics.common.entity.order.PaymentMethod.COD
                    AND d.order.status = com.cosmetics.common.entity.order.OrderStatus.DELIVERED)
                OR
                (d.order.paymentMethod = com.cosmetics.common.entity.order.PaymentMethod.PAYPAL)
          )
        """)
    List<OrderDetail> findWithCategoryAndTimeBetween(@Param("startTime") Date startTime,
                                                     @Param("endTime") Date endTime);



    @Query("""
        SELECT DISTINCT d
        FROM OrderDetail d
        JOIN FETCH d.variant v
        JOIN FETCH v.product
        WHERE d.order.orderTime BETWEEN :startTime AND :endTime
            AND d.order.status NOT IN (
                 com.cosmetics.common.entity.order.OrderStatus.CANCELLED,
                 com.cosmetics.common.entity.order.OrderStatus.RETURNED,
                 com.cosmetics.common.entity.order.OrderStatus.REFUNDED
            )
            AND (
                 (d.order.paymentMethod = com.cosmetics.common.entity.order.PaymentMethod.COD
                      AND d.order.status = com.cosmetics.common.entity.order.OrderStatus.DELIVERED)
                 OR
                 (d.order.paymentMethod = com.cosmetics.common.entity.order.PaymentMethod.PAYPAL)
          )
        """)
    List<OrderDetail> findWithProductAndTimeBetween(@Param("startTime") Date startTime,
                                                    @Param("endTime") Date endTime);


    @Query("""
    SELECT DISTINCT d
    FROM OrderDetail d
    JOIN FETCH d.variant v
    JOIN FETCH v.product p
    LEFT JOIN FETCH p.category
    WHERE d.order.orderTime BETWEEN :startTime AND :endTime
      AND (:statuses IS NULL OR d.order.status IN :statuses)
      AND (:payments IS NULL OR d.order.paymentMethod IN :payments)
    """)
    List<OrderDetail> findWithCategoryAndTimeBetweenFiltered(
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("statuses") List<com.cosmetics.common.entity.order.OrderStatus> statuses,
            @Param("payments") List<com.cosmetics.common.entity.order.PaymentMethod> payments);

    @Query("""
    SELECT DISTINCT d
    FROM OrderDetail d
    JOIN FETCH d.variant v
    JOIN FETCH v.product p
    WHERE d.order.orderTime BETWEEN :startTime AND :endTime
      AND (:statuses IS NULL OR d.order.status IN :statuses)
      AND (:payments IS NULL OR d.order.paymentMethod IN :payments)
""")
    List<OrderDetail> findWithProductAndTimeBetweenFiltered(
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("statuses") List<com.cosmetics.common.entity.order.OrderStatus> statuses,
            @Param("payments") List<com.cosmetics.common.entity.order.PaymentMethod> payments);

}
