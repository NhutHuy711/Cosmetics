package com.cosmetics.common.entity.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.cosmetics.common.entity.IdBasedEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "option_values", uniqueConstraints = {
        @UniqueConstraint(name = "uk_ov_type_value", columnNames = {"option_type_id", "value"})
})
public class OptionValue extends IdBasedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_type_id", nullable = false)
    private OptionType optionType;

    @Column(length = 64, nullable = false)
    private String value;

    @ManyToMany(mappedBy = "optionValues")
    private Set<ProductVariant> variants = new HashSet<>();

    public OptionValue() {
    }

    public OptionValue(Integer id) {
        this.id = id;
    }

    public OptionValue(OptionType optionType, String value) {
        this.optionType = optionType;
        this.value = value;
    }

    @JsonIgnore
    public OptionType getOptionType() {
        return optionType;
    }

    public void setOptionType(OptionType optionType) {
        this.optionType = optionType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @JsonIgnore
    public Set<ProductVariant> getVariants() {
        return variants;
    }

    public void setVariants(Set<ProductVariant> variants) {
        this.variants = variants;
    }
}
