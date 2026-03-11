package com.cosmetics.common.entity.product;

import com.cosmetics.common.entity.IdBasedEntity;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "option_types")
public class OptionType extends IdBasedEntity {

    @Column(length = 64, nullable = false)
    private String name;

    @OneToMany(mappedBy = "optionType", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OptionValue> values = new LinkedHashSet<>();

    public OptionType() {
    }

    public OptionType(Integer id) {
        this.id = id;
    }

    public OptionType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<OptionValue> getValues() {
        return values;
    }

    public void setValues(Set<OptionValue> values) {
        this.values = values;
    }
}
