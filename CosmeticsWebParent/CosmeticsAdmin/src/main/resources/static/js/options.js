(function () {
    const SUMMARY_BASE_CLASS = 'alert py-2 px-3 mb-3';
    const SAVE_FEEDBACK_BASE_CLASS = 'alert mt-3';

    const resolveApiUrl = () => {
        if (typeof optionsApiUrl === 'string' && optionsApiUrl.length > 0) {
            return optionsApiUrl;
        }
        if (typeof contextPath === 'string' && contextPath.length > 0) {
            return `${contextPath}options/api`;
        }
        return '/options/api';
    };

    const csrfHeader = (typeof csrfHeaderName === 'string' && csrfHeaderName.length > 0)
        ? csrfHeaderName
        : 'X-CSRF-TOKEN';
    const csrfToken = (typeof csrfValue === 'string') ? csrfValue : '';

    const updateSummary = (summaryElement, message, type) => {
        if (!summaryElement) {
            return;
        }
        summaryElement.className = `${SUMMARY_BASE_CLASS} alert-${type}`;
        summaryElement.innerHTML = message;
    };

    const resetVariantSummary = (summaryElement) => {
        updateSummary(
            summaryElement,
            'No variants generated yet. Add at least one attribute and press <strong>Generate variants</strong> to calculate the total number of combinations.',
            'info'
        );
    };

    const updateSaveFeedback = (feedbackElement, message, type) => {
        if (!feedbackElement) {
            return;
        }

        if (!message) {
            feedbackElement.textContent = '';
            feedbackElement.className = `${SAVE_FEEDBACK_BASE_CLASS} d-none`;
            return;
        }

        feedbackElement.className = `${SAVE_FEEDBACK_BASE_CLASS} alert-${type}`;
        feedbackElement.innerHTML = message;
    };

    const clearValidation = (input, feedbackElement) => {
        if (input) {
            input.classList.remove('is-invalid');
        }
        if (feedbackElement) {
            feedbackElement.textContent = '';
        }
    };

    const parseInteger = (value) => {
        if (value === null || value === undefined || value === '') {
            return null;
        }
        const parsed = Number.parseInt(value, 10);
        return Number.isNaN(parsed) ? null : parsed;
    };

    const summarizeAttributes = (attributes) => {
        if (!Array.isArray(attributes)) {
            return { attributeCount: 0, valueCount: 0 };
        }

        const attributeCount = attributes.length;
        const valueCount = attributes.reduce((total, attribute) => {
            if (!attribute || !Array.isArray(attribute.values)) {
                return total;
            }
            return total + attribute.values.length;
        }, 0);

        return { attributeCount, valueCount };
    };

    const collectAttributes = (container) => {
        const groups = container.querySelectorAll('.attribute-group');
        const attributes = [];
        const seenNames = new Set();
        let hasInvalid = false;

        groups.forEach((group) => {
            const attributeId = parseInteger(group.dataset.attributeId);
            const nameInput = group.querySelector('.attr-name');
            const nameFeedback = group.querySelector('.name-feedback');
            const valuesInput = group.querySelector('.attr-value-input');
            const valueFeedback = group.querySelector('.value-feedback');
            const valuesList = group.querySelector('.attribute-values-list');

            clearValidation(nameInput, nameFeedback);
            clearValidation(valuesInput, valueFeedback);

            const name = nameInput ? nameInput.value.trim() : '';
            const normalizedName = name.toLowerCase();

            const chips = valuesList ? Array.from(valuesList.querySelectorAll('.value-chip')) : [];
            const values = [];
            const seenValues = new Set();

            let attributeInvalid = false;

            if (!name) {
                attributeInvalid = true;
                if (nameInput) {
                    nameInput.classList.add('is-invalid');
                }
                if (nameFeedback) {
                    nameFeedback.textContent = 'Enter a name for this attribute.';
                }
            } else if (seenNames.has(normalizedName)) {
                attributeInvalid = true;
                if (nameInput) {
                    nameInput.classList.add('is-invalid');
                }
                if (nameFeedback) {
                    nameFeedback.textContent = 'This attribute name is already used.';
                }
            } else {
                seenNames.add(normalizedName);
            }

            if (!chips.length) {
                attributeInvalid = true;
                if (valuesInput) {
                    valuesInput.classList.add('is-invalid');
                }
                if (valueFeedback) {
                    valueFeedback.textContent = 'Add at least one value for this attribute.';
                }
            }

            chips.forEach((chip) => {
                const valueText = (chip.dataset.value || '').trim();
                if (!valueText) {
                    return;
                }
                const normalizedValue = valueText.toLowerCase();
                if (seenValues.has(normalizedValue)) {
                    attributeInvalid = true;
                    if (valuesInput) {
                        valuesInput.classList.add('is-invalid');
                    }
                    if (valueFeedback && !valueFeedback.textContent) {
                        valueFeedback.textContent = 'Duplicate values are not allowed for the same attribute.';
                    }
                    return;
                }

                seenValues.add(normalizedValue);
                const valueId = parseInteger(chip.dataset.valueId);
                values.push({ id: valueId, value: valueText });
            });

            if (!attributeInvalid && name && values.length) {
                attributes.push({
                    id: attributeId,
                    name,
                    values
                });
            }

            hasInvalid = hasInvalid || attributeInvalid;
        });

        return {
            attributes,
            hasInvalid,
            groupCount: groups.length
        };
    };

    const parseErrorMessage = async (response) => {
        try {
            const data = await response.json();
            if (data) {
                if (typeof data.message === 'string' && data.message.trim().length > 0) {
                    return data.message;
                }
                if (Array.isArray(data.errors) && data.errors.length > 0) {
                    return data.errors.map((error) => String(error)).join('<br>');
                }
            }
        } catch (error) {
            // Ignore JSON parsing failures and fall back to status text
        }
        return response.statusText || `Request failed with status ${response.status}`;
    };

    document.addEventListener('DOMContentLoaded', () => {
        const attributesContainer = document.getElementById('attributes');
        const addAttributeBtn = document.getElementById('addAttribute');
        const generateBtn = document.getElementById('generate');
        const saveBtn = document.getElementById('saveAttributes');
        const attributeCounter = document.getElementById('attributeCounter');
        const variantSummary = document.getElementById('variantSummary');
        const saveFeedback = document.getElementById('saveFeedback');

        if (!attributesContainer || !addAttributeBtn || !generateBtn || !saveBtn) {
            return;
        }

        const apiUrl = resolveApiUrl();

        const updateAttributeCounter = () => {
            const count = attributesContainer.querySelectorAll('.attribute-group').length;
            if (attributeCounter) {
                attributeCounter.textContent = String(count);
            }
        };

        const addAttributeGroup = (attribute) => {
            const wrapper = document.createElement('div');
            wrapper.className = 'attribute-group border p-3 rounded-3 bg-white shadow-sm';

            if (attribute && attribute.id != null) {
                wrapper.dataset.attributeId = String(attribute.id);
            }

            wrapper.innerHTML = `
                <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
                    <div class="flex-grow-1">
                        <label class="form-label fw-semibold">Attribute name</label>
                        <input type="text" class="form-control attr-name" placeholder="e.g., Color" maxlength="64">
                        <div class="invalid-feedback name-feedback"></div>
                    </div>
                    <button type="button" class="btn btn-outline-danger btn-sm remove-attr" title="Remove attribute">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
                <div>
                    <label class="form-label fw-semibold">Values</label>
                    <div class="input-group">
                        <input type="text" class="form-control attr-value-input" placeholder="Type a value and press Enter" maxlength="64">
                        <button type="button" class="btn btn-outline-secondary add-value" title="Add value">
                            <i class="bi bi-plus-lg"></i>
                        </button>
                    </div>
                    <div class="invalid-feedback value-feedback"></div>
                    <div class="d-flex flex-wrap gap-2 mt-3 attribute-values-list"></div>
                    <div class="form-text text-muted">Press Enter or use the "+" icon to add each value individually.</div>
                </div>
            `;

            const nameInput = wrapper.querySelector('.attr-name');
            const nameFeedback = wrapper.querySelector('.name-feedback');
            const valuesInput = wrapper.querySelector('.attr-value-input');
            const valuesList = wrapper.querySelector('.attribute-values-list');
            const addValueButton = wrapper.querySelector('.add-value');
            const removeButton = wrapper.querySelector('.remove-attr');
            const valueFeedback = wrapper.querySelector('.value-feedback');

            const hasDuplicateValue = (value) => {
                if (!valuesList) {
                    return false;
                }
                const normalized = value.toLowerCase();
                return Array.from(valuesList.querySelectorAll('.value-chip')).some((chip) => {
                    return (chip.dataset.value || '').toLowerCase() === normalized;
                });
            };

            const createValueChip = (value, valueId) => {
                const chip = document.createElement('span');
                chip.className = 'badge bg-light text-dark border rounded-pill d-inline-flex align-items-center gap-2 px-3 py-2 value-chip';
                chip.dataset.value = value;
                if (valueId != null) {
                    chip.dataset.valueId = String(valueId);
                }

                const label = document.createElement('span');
                label.textContent = value;

                const removeBtn = document.createElement('button');
                removeBtn.type = 'button';
                removeBtn.className = 'btn-close btn-close-sm remove-value';
                removeBtn.setAttribute('aria-label', `Remove value ${value}`);
                removeBtn.addEventListener('click', () => {
                    chip.remove();
                    clearValidation(valuesInput, valueFeedback);
                    if (valuesInput) {
                        valuesInput.focus();
                    }
                });

                chip.appendChild(label);
                chip.appendChild(removeBtn);
                return chip;
            };

            const addValue = () => {
                if (!valuesInput || !valuesList) {
                    return;
                }

                const rawValue = valuesInput.value.trim();
                const normalizedWhitespace = rawValue.replace(/\s+/g, ' ');

                if (!normalizedWhitespace) {
                    valuesInput.classList.add('is-invalid');
                    if (valueFeedback) {
                        valueFeedback.textContent = 'Enter a value before adding it.';
                    }
                    return;
                }

                if (hasDuplicateValue(normalizedWhitespace)) {
                    valuesInput.classList.add('is-invalid');
                    if (valueFeedback) {
                        valueFeedback.textContent = 'This value already exists for the attribute.';
                    }
                    return;
                }

                valuesList.appendChild(createValueChip(normalizedWhitespace));
                valuesInput.value = '';
                clearValidation(valuesInput, valueFeedback);
                valuesInput.focus();
            };

            if (nameInput) {
                nameInput.addEventListener('input', () => clearValidation(nameInput, nameFeedback));
                if (attribute && typeof attribute.name === 'string') {
                    nameInput.value = attribute.name;
                }
            }

            if (valuesInput) {
                valuesInput.addEventListener('input', () => clearValidation(valuesInput, valueFeedback));
                valuesInput.addEventListener('keydown', (event) => {
                    if (event.key === 'Enter') {
                        event.preventDefault();
                        addValue();
                    }
                });
            }

            if (addValueButton) {
                addValueButton.addEventListener('click', (event) => {
                    event.preventDefault();
                    addValue();
                });
            }

            if (removeButton) {
                removeButton.addEventListener('click', () => {
                    wrapper.remove();
                    updateAttributeCounter();
                    if (!attributesContainer.querySelector('.attribute-group')) {
                        resetVariantSummary(variantSummary);
                    }
                });
            }

            if (attribute && Array.isArray(attribute.values) && valuesList) {
                const sortedValues = [...attribute.values].sort((a, b) => {
                    const valueA = (a && typeof a.value === 'string') ? a.value : '';
                    const valueB = (b && typeof b.value === 'string') ? b.value : '';
                    return valueA.localeCompare(valueB, undefined, { sensitivity: 'base' });
                });

                sortedValues.forEach((value) => {
                    if (value && typeof value.value === 'string') {
                        valuesList.appendChild(createValueChip(value.value, value.id));
                    }
                });
            }

            attributesContainer.appendChild(wrapper);
            updateAttributeCounter();
            return nameInput;
        };

        const renderAttributes = (attributes) => {
            attributesContainer.innerHTML = '';
            if (Array.isArray(attributes) && attributes.length > 0) {
                const sortedAttributes = [...attributes].sort((a, b) => {
                    const nameA = (a && typeof a.name === 'string') ? a.name : '';
                    const nameB = (b && typeof b.name === 'string') ? b.name : '';
                    return nameA.localeCompare(nameB, undefined, { sensitivity: 'base' });
                });

                let firstInput = null;
                sortedAttributes.forEach((attribute, index) => {
                    const input = addAttributeGroup(attribute);
                    if (index === 0) {
                        firstInput = input;
                    }
                });
                if (firstInput) {
                    firstInput.focus();
                }
            } else {
                const input = addAttributeGroup();
                if (input) {
                    input.focus();
                }
            }
        };

        const loadDefinitions = async () => {
            updateSummary(variantSummary, 'Loading saved attributes…', 'info');
            try {
                const response = await fetch(apiUrl, {
                    headers: {
                        Accept: 'application/json'
                    }
                });

                if (!response.ok) {
                    throw new Error('Failed to load saved attributes.');
                }

                const data = await response.json();
                const attributes = Array.isArray(data) ? data : [];

                renderAttributes(attributes);

                const { attributeCount, valueCount } = summarizeAttributes(attributes);

                if (attributeCount) {
                    updateSummary(
                        variantSummary,
                        `Loaded <strong>${attributeCount}</strong> saved attributes with <strong>${valueCount}</strong> total values.`,
                        'info'
                    );
                } else {
                    updateSummary(
                        variantSummary,
                        'No saved attributes yet. Start by adding your first attribute below.',
                        'info'
                    );
                }
            } catch (error) {
                renderAttributes([]);
                updateSummary(
                    variantSummary,
                    'Unable to load saved attributes. Add new attributes and save to create your first set.',
                    'warning'
                );
            }
        };

        addAttributeBtn.addEventListener('click', () => {
            const nameInput = addAttributeGroup();
            if (nameInput) {
                nameInput.focus();
            }
        });

        generateBtn.addEventListener('click', () => {
            const { attributes, hasInvalid, groupCount } = collectAttributes(attributesContainer);

            if (groupCount === 0) {
                updateSummary(variantSummary, 'Add at least one attribute before generating variants.', 'warning');
                return;
            }

            if (hasInvalid) {
                updateSummary(
                    variantSummary,
                    'Complete the name and values for each attribute before generating variants.',
                    'warning'
                );
                return;
            }

            if (!attributes.length) {
                updateSummary(variantSummary, 'No valid attributes were provided to generate variants.', 'warning');
                return;
            }

            const totalVariants = attributes.reduce((total, attribute) => total * attribute.values.length, 1);
            const attributeCount = attributes.length;

            const message = totalVariants === 1
                ? 'Generated <strong>1</strong> variant from a single attribute.'
                : `Generated <strong>${totalVariants}</strong> variants across <strong>${attributeCount}</strong> attributes.`;

            updateSummary(variantSummary, message, 'success');
        });

        saveBtn.addEventListener('click', async () => {
            const { attributes, hasInvalid, groupCount } = collectAttributes(attributesContainer);

            if (groupCount === 0) {
                updateSaveFeedback(saveFeedback, 'Add at least one attribute before saving.', 'warning');
                return;
            }

            if (hasInvalid || !attributes.length) {
                updateSaveFeedback(saveFeedback, 'Resolve validation issues before saving your changes.', 'warning');
                return;
            }

            saveBtn.disabled = true;
            const originalLabel = saveBtn.dataset.originalLabel || saveBtn.textContent;
            saveBtn.dataset.originalLabel = originalLabel;
            saveBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Saving...';
            updateSaveFeedback(saveFeedback, '', 'info');

            try {
                const response = await fetch(apiUrl, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        Accept: 'application/json',
                        [csrfHeader]: csrfToken
                    },
                    body: JSON.stringify(attributes)
                });

                if (!response.ok) {
                    const message = await parseErrorMessage(response);
                    throw new Error(message);
                }

                const data = await response.json();
                const savedAttributes = Array.isArray(data) ? data : [];

                renderAttributes(savedAttributes);

                const { attributeCount, valueCount } = summarizeAttributes(savedAttributes);

                if (attributeCount) {
                    updateSaveFeedback(
                        saveFeedback,
                        `Saved <strong>${attributeCount}</strong> attributes with <strong>${valueCount}</strong> total values.`,
                        'success'
                    );
                } else {
                    updateSaveFeedback(saveFeedback, 'All attributes have been cleared.', 'success');
                }

                updateSummary(
                    variantSummary,
                    'Definitions saved successfully. Use Generate variants to recalculate combinations.',
                    'success'
                );
            } catch (error) {
                updateSaveFeedback(saveFeedback, error.message || 'Unable to save attributes.', 'danger');
            } finally {
                saveBtn.disabled = false;
                saveBtn.textContent = saveBtn.dataset.originalLabel || 'Save attributes';
            }
        });

        resetVariantSummary(variantSummary);
        loadDefinitions();
    });
})();
