package com.cosmetics.admin.setting.state;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.cosmetics.common.entity.Country;
import com.cosmetics.common.entity.State;

public interface StateRepository extends CrudRepository<State, Integer> {

    public List<State> findByCountryOrderByNameAsc(Country country);
}
