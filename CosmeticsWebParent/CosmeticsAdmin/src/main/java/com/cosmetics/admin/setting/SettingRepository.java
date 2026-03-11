package com.cosmetics.admin.setting;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.cosmetics.common.entity.setting.Setting;
import com.cosmetics.common.entity.setting.SettingCategory;

public interface SettingRepository extends CrudRepository<Setting, String> {
    public List<Setting> findByCategory(SettingCategory category);
}
