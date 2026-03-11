package com.cosmetics.setting;

import org.springframework.data.repository.CrudRepository;

import com.cosmetics.common.entity.Currency;

public interface CurrencyRepository extends CrudRepository<Currency, Integer> {

}
