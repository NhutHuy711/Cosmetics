package com.cosmetics.admin.productVariant;

import com.cosmetics.common.entity.product.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductVariantRepository extends PagingAndSortingRepository<ProductVariant, Integer> {
    @Query("""
    SELECT v FROM ProductVariant v
    JOIN v.product p
    WHERE (p.category.id = ?1
           OR p.category.allParentIDs LIKE %?2%)
      AND (p.name LIKE %?3%
           OR p.fullDescription LIKE %?3%
           OR p.brand.name LIKE %?3%
           OR p.category.name LIKE %?3%)
""")
    Page<ProductVariant> searchInCategory(Integer categoryId, String categoryIdMatch, String keyword, Pageable pageable);

    @Query("""
    SELECT v
    FROM ProductVariant v
    JOIN v.product p
    LEFT JOIN p.brand b
    LEFT JOIN p.category c
    WHERE v.enabled = true
      AND (
           LOWER(v.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(p.fullDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
""")
    Page<ProductVariant> findAll(@Param("keyword") String keyword, Pageable pageable);


    @Query("SELECT NEW ProductVariant (p.id, p.name) FROM ProductVariant p ORDER BY p.name ASC")
    public List<ProductVariant> findAll();

    @Query("""
    SELECT v FROM ProductVariant v
    JOIN v.product p
    WHERE (p.category.id = ?1
           OR p.category.allParentIDs LIKE %?2%)
""")
    Page<ProductVariant> findAllInCategory(Integer categoryId, String categoryIdMatch, Pageable pageable);

    @Query("UPDATE ProductVariant v SET v.enabled = ?2 WHERE v.id = ?1")
    @Modifying
    public void updateEnabledStatus(Integer id, boolean enabled);

    public List<ProductVariant> findByIdIn(List<Integer> ids);

    @Query("""
    SELECT v FROM ProductVariant v
    JOIN v.product p
    WHERE LOWER(v.name) LIKE CONCAT('%', :keyword, '%')
       OR LOWER(p.name) LIKE CONCAT('%', :keyword, '%')
       OR str(v.id) LIKE CONCAT('%', :keyword, '%')
       OR str(p.id) LIKE CONCAT('%', :keyword, '%')
""")
    Page<ProductVariant> searchForPromotion(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT v FROM ProductVariant v JOIN FETCH v.product")
    List<ProductVariant> findAllWithProduct();

    @Query("""
       SELECT v FROM ProductVariant v
       JOIN FETCH v.product p
       LEFT JOIN FETCH p.brand
       LEFT JOIN FETCH p.category
       """)
    List<ProductVariant> findAllWithProductBrandCategory();

    @Query("""
        SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END
        FROM ProductVariant v
        WHERE v.product.id = :productId
          AND (
                SELECT COUNT(ovAll)
                FROM ProductVariant vAll
                JOIN vAll.optionValues ovAll
                WHERE vAll.id = v.id
          ) = :size
          AND (
                SELECT COUNT(DISTINCT ovIn.id)
                FROM ProductVariant vIn
                JOIN vIn.optionValues ovIn
                WHERE vIn.id = v.id
                  AND ovIn.id IN :valueIds
          ) = :size
    """)
    boolean existsDuplicateExactOptions(@Param("productId") Integer productId,
                                        @Param("valueIds") List<Integer> valueIds,
                                        @Param("size") long size);
}
