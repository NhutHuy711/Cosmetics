package com.cosmetics.admin.promotion;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.cosmetics.common.entity.promotion.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.cosmetics.admin.paging.SearchRepository;
import org.springframework.data.repository.query.Param;

public interface PromotionRepository extends SearchRepository<Promotion, Integer> {

    @Query("SELECT DISTINCT p FROM Promotion p LEFT JOIN p.promotionProducts pp "
            + "WHERE p.name LIKE %?1% "
            + "OR CAST(pp.percentOff AS string) LIKE %?1% "
            + "OR FUNCTION('DATE_FORMAT', p.startAt, '%Y-%m-%d') LIKE %?1% "
            + "OR FUNCTION('DATE_FORMAT', p.endAt, '%Y-%m-%d') LIKE %?1%")
    public Page<Promotion> findAll(String keyword, Pageable pageable);

    public List<Promotion> findAll();

    @Query("UPDATE Promotion p SET p.enabled = ?2 WHERE p.id = ?1")
    @Modifying
    public void updateEnabledStatus(Integer id, boolean enabled);

    @Query("SELECT DISTINCT p FROM Promotion p "
            + "LEFT JOIN FETCH p.promotionProducts pp "
            + "LEFT JOIN FETCH pp.variant v "
            + "LEFT JOIN FETCH v.product prod "
            + "WHERE p.id = ?1")
    Optional<Promotion> findByIdWithVariants(Integer id);

    @Query("""
        select 
            pp.variant.id,   
            p.id,             
            p.name,           
            p.startAt,        
            p.endAt           
        from PromotionProduct pp 
        join pp.promotion p
        where pp.variant.id in :variantIds
          and p.enabled = true
          and (p.startAt < :endAt and :startAt < p.endAt)
          and (:excludeId is null or p.id <> :excludeId)
    """)
    List<Object[]> findOverlapsRaw(
            @Param("variantIds") Collection<Integer> variantIds,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("excludeId") Integer excludeId
    );
}
