package com.cosmetics.productVariant;

import com.cosmetics.common.entity.product.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer>,
        JpaSpecificationExecutor<ProductVariant> {

    @Query("""
    SELECT v FROM ProductVariant v
    JOIN v.product p
    JOIN p.brand b
    JOIN p.category c
    WHERE v.enabled = true
      AND p.enabled = true
      AND b.enabled = true
      AND c.enabled = true
    ORDER BY v.createdAt DESC
""")
    Page<ProductVariant> findNewVariants(Pageable pageable);

    @Query("""
    SELECT v FROM ProductVariant v
    JOIN v.product p
    JOIN p.brand b
    JOIN p.category c
    JOIN v.promotionProducts pp
    JOIN pp.promotion promo
    WHERE v.enabled = true
      AND p.enabled = true
      AND b.enabled = true
      AND c.enabled = true
      AND promo.enabled = true
      AND CURRENT_TIMESTAMP BETWEEN promo.startAt AND promo.endAt
    GROUP BY v.id
    ORDER BY MAX(pp.percentOff) DESC
""")
    Page<ProductVariant> findSpecialOffers(Pageable pageable);

    @Query("""
    SELECT pv FROM ProductVariant pv
    JOIN pv.product p
    JOIN p.brand b
    JOIN p.category c
    LEFT JOIN OrderDetail od ON pv.id = od.variant.id
    LEFT JOIN od.order o
    WHERE pv.enabled = true
      AND p.enabled = true
      AND b.enabled = true
      AND c.enabled = true
      AND (o.status = 'DELIVERED' OR o.status IS NULL)
    GROUP BY pv.id
""")
    Page<ProductVariant> findBestSellingVariants(Pageable pageable);


    @Query(
            value = """
        SELECT v FROM ProductVariant v
        JOIN v.product p
        JOIN p.category c
        LEFT JOIN v.orderDetails od
        LEFT JOIN od.order o
        WHERE p.enabled = true
          AND (c.id = ?1 OR c.allParentIDs LIKE CONCAT('%', ?2, '%'))
        GROUP BY v
        ORDER BY COALESCE(SUM(
            CASE WHEN o.status = 'DELIVERED' THEN od.quantity ELSE 0 END
        ), 0) DESC
    """,
            countQuery = """
        SELECT COUNT(v) FROM ProductVariant v
        JOIN v.product p
        JOIN p.category c
        WHERE p.enabled = true
          AND (c.id = ?1 OR c.allParentIDs LIKE CONCAT('%', ?2, '%'))
    """
    )
    Page<ProductVariant> findAllOrderByMostSold(Integer categoryId, String categoryIDMatch, Pageable pageable);

    @Query("""
    SELECT v FROM ProductVariant v
    JOIN v.product p
    JOIN p.brand b
    LEFT JOIN v.orderDetails od
    LEFT JOIN od.order o
    WHERE b.id = ?1
      AND p.enabled = true
      AND v.enabled = true
      AND b.enabled = true
    GROUP BY v.id
    ORDER BY COALESCE(SUM(CASE WHEN o.status = 'DELIVERED' THEN od.quantity ELSE 0 END), 0) DESC
""")
    Page<ProductVariant> findAllOrderByMostSoldByBrand(Integer brandId, Pageable pageable);

    public ProductVariant findByAlias(String alias);

    @Query("SELECT v FROM ProductVariant v " +
            "JOIN FETCH v.product p " +
            "LEFT JOIN FETCH v.optionValues ov " +
            "LEFT JOIN FETCH ov.optionType " +
            "WHERE p.id = ?1 AND v.enabled = true " +
            "ORDER BY v.id ASC")
    List<ProductVariant> findByProductId(Integer productId);

    @Query("""
    SELECT DISTINCT pv 
    FROM ProductVariant pv
    JOIN FETCH pv.product p
    LEFT JOIN FETCH pv.optionValues ov
    LEFT JOIN FETCH ov.optionType
    LEFT JOIN FETCH pv.images
    WHERE pv.alias = ?1
    """)
    ProductVariant findByAliasWithOptions(String alias);

    @Query("""
SELECT DISTINCT v 
FROM ProductVariant v
JOIN FETCH v.product p
LEFT JOIN FETCH v.optionValues ov
LEFT JOIN FETCH ov.optionType
LEFT JOIN FETCH v.images
WHERE p.id = ?1 AND v.enabled = true
ORDER BY v.id ASC
""")
    List<ProductVariant> findVariantsWithOptionsByProductId(Integer productId);

}
