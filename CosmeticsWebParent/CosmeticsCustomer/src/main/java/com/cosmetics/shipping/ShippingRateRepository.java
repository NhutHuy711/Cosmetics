package com.cosmetics.shipping;

import org.springframework.data.repository.CrudRepository;

import com.cosmetics.common.entity.Country;
import com.cosmetics.common.entity.ShippingRate;

public interface ShippingRateRepository extends CrudRepository<ShippingRate, Integer> {

    public ShippingRate findByCountryAndState(Country country, String state);
}
