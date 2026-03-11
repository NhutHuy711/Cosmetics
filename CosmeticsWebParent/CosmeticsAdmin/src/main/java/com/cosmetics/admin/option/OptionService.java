package com.cosmetics.admin.option;

import com.cosmetics.common.entity.product.OptionType;
import com.cosmetics.common.entity.product.OptionValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OptionService {

    @Autowired
    private OptionTypeRepository optionTypeRepository;

    @Autowired
    private OptionValueRepository valueRepo;


    public OptionService(OptionTypeRepository optionTypeRepository) {
        this.optionTypeRepository = optionTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<OptionType> fetchDefinitions() {
        List<OptionType> optionTypes = optionTypeRepository.findAll();
        return prepareForResponse(optionTypes);
    }

    @Transactional
    public List<OptionType> saveDefinitions(List<OptionType> attributeDefinitions) {
        if (CollectionUtils.isEmpty(attributeDefinitions)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one attribute is required.");
        }

        Map<Integer, OptionType> existingById = optionTypeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(OptionType::getId, Function.identity()));

        Set<String> seenNames = new HashSet<>();
        for (OptionType attribute : attributeDefinitions) {
            String name = attribute.getName();
            if (name != null) {
                name = name.trim();
            }
            if (name == null || name.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each attribute requires a name.");
            }
            String normalizedName = name.toLowerCase(Locale.ENGLISH);
            if (!seenNames.add(normalizedName)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Duplicate attribute name '%s'.", name));
            }

            OptionType optionType;
            Integer attributeId = attribute.getId();
            if (attributeId != null) {
                optionType = existingById.remove(attributeId);
                if (optionType == null) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                            String.format("Attribute with id %d was not found.", attributeId));
                }
                ensureNameIsUniqueForUpdate(optionType, name);
            } else {
                ensureNameIsUniqueForCreate(name);
                optionType = new OptionType();
            }

            optionType.setName(name);
            syncValues(optionType, attribute.getValues());

            optionTypeRepository.save(optionType);
        }

        removeAbsentAttributes(existingById.values());

        optionTypeRepository.flush();

        List<OptionType> updatedOptionTypes = optionTypeRepository.findAll();
        return prepareForResponse(updatedOptionTypes);
    }

    private void ensureNameIsUniqueForCreate(String name) {
        if (optionTypeRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Attribute name '%s' already exists.", name));
        }
    }

    private void ensureNameIsUniqueForUpdate(OptionType currentType, String name) {
        optionTypeRepository.findByNameIgnoreCase(name)
                .filter(existing -> !Objects.equals(existing.getId(), currentType.getId()))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            String.format("Attribute name '%s' is already used by another record.", name));
                });
    }

    private void syncValues(OptionType optionType, Collection<OptionValue> incomingValues) {
        if (CollectionUtils.isEmpty(incomingValues)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Attribute '%s' must include at least one value.", optionType.getName()));
        }

        Map<Integer, OptionValue> existingById = optionType.getValues()
                .stream()
                .filter(value -> value.getId() != null)
                .collect(Collectors.toMap(OptionValue::getId, Function.identity()));

        Set<String> seenValues = new HashSet<>();
        Set<Integer> seenIds = new HashSet<>();

        for (OptionValue incomingValue : incomingValues) {
            String valueText = incomingValue.getValue();
            if (valueText != null) {
                valueText = valueText.trim();
            }
            if (valueText == null || valueText.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Attribute '%s' cannot contain blank values.", optionType.getName()));
            }
            String normalizedValue = valueText.toLowerCase(Locale.ENGLISH);
            if (!seenValues.add(normalizedValue)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Duplicate value '%s' for attribute '%s'.", valueText, optionType.getName()));
            }

            OptionValue optionValue;
            Integer valueId = incomingValue.getId();
            if (valueId != null) {
                if (!seenIds.add(valueId)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            String.format("Duplicate value identifier %d for attribute '%s'.", valueId, optionType.getName()));
                }
                optionValue = existingById.remove(valueId);
                if (optionValue == null) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                            String.format("Value with id %d was not found for attribute '%s'.", valueId, optionType.getName()));
                }
                optionValue.setValue(valueText);
            } else {
                optionValue = new OptionValue(optionType, valueText);
                optionType.getValues().add(optionValue);
            }
            optionValue.setOptionType(optionType);
        }

        for (OptionValue valueToRemove : existingById.values()) {
            optionType.getValues().remove(valueToRemove);
        }
    }

    private void removeAbsentAttributes(Collection<OptionType> optionTypes) {

        for (OptionType optionType : optionTypes) {

            Integer typeId = optionType.getId();

            // 1) Kiểm tra OptionType đang được sử dụng không
            long usageCount = valueRepo.countUsageByOptionType(typeId);

            if (usageCount > 0) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        String.format(
                                "Attribute '%s' cannot be removed because it is used by %d variant(s).",
                                optionType.getName(), usageCount
                        )
                );
            }

            // 2) Xoá OptionType
            try {
                optionTypeRepository.deleteById(typeId);  // <-- an toàn hơn delete(optionType)
            } catch (EmptyResultDataAccessException ex) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format(
                                "Attribute with id %d could not be removed because it no longer exists.",
                                typeId
                        )
                );
            }
        }
    }

    private List<OptionType> prepareForResponse(List<OptionType> optionTypes) {
        return optionTypes.stream()
                .sorted(Comparator.comparing(OptionType::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::copyForResponse)
                .collect(Collectors.toList());
    }

    private OptionType copyForResponse(OptionType source) {
        OptionType copy = new OptionType();
        copy.setId(source.getId());
        copy.setName(source.getName());

        Set<OptionValue> sortedValues = source.getValues()
                .stream()
                .sorted(Comparator.comparing(OptionValue::getValue, String.CASE_INSENSITIVE_ORDER))
                .map(value -> {
                    OptionValue detached = new OptionValue();
                    detached.setId(value.getId());
                    detached.setValue(value.getValue());
                    return detached;
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));

        copy.setValues(sortedValues);
        return copy;
    }

    public List<OptionType> listAll() {
        return optionTypeRepository.findAll();
    }

    public List<OptionType> listAllWithValue() {
        return optionTypeRepository.findAllWithValues();
    }

    public List<OptionValue> listByType(Integer typeId) {
        return valueRepo.findByOptionTypeId(typeId);
    }
}
