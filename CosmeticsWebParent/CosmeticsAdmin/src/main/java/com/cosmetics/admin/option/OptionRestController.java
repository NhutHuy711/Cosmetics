package com.cosmetics.admin.option;

import com.cosmetics.common.entity.product.OptionType;
import com.cosmetics.common.entity.product.OptionValue;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/options/api")
public class OptionRestController {

    private final OptionService optionService;

    public OptionRestController(OptionService optionService) {
        this.optionService = optionService;
    }

    @GetMapping
    public List<OptionType> listDefinitions() {
        return optionService.fetchDefinitions();
    }

    @PostMapping
    public List<OptionType> saveDefinitions(@RequestBody List<OptionType> request) {
        return optionService.saveDefinitions(request);
    }

    @GetMapping("/{typeId}")
    public List<OptionValue> listValues(@PathVariable Integer typeId) {
        return optionService.listByType(typeId);
    }
}
