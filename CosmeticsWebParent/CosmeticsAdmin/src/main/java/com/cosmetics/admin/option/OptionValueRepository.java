package com.cosmetics.admin.option;

import com.cosmetics.common.entity.product.OptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OptionValueRepository extends JpaRepository<OptionValue, Integer> {
    List<OptionValue> findByOptionTypeId(Integer optionTypeId);

    @Query("SELECT COUNT(ov) FROM OptionValue ov " +
            "JOIN ov.variants v " +
            "WHERE ov.optionType.id = ?1")
    public Long countUsageByOptionType(Integer typeId);

}
