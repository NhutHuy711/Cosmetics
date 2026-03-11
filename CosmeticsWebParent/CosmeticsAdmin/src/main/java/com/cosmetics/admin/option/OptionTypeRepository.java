package com.cosmetics.admin.option;

import com.cosmetics.common.entity.product.OptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OptionTypeRepository extends JpaRepository<OptionType, Integer> {

    Optional<OptionType> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT DISTINCT t FROM OptionType t LEFT JOIN FETCH t.values")
    List<OptionType> findAllWithValues();

}
