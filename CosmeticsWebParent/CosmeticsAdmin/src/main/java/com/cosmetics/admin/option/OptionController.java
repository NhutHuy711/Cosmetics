package com.cosmetics.admin.option;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OptionController {

    @GetMapping("/options")
    public String showOptionsPage(Model model) {
        model.addAttribute("moduleURL", "/options");
        model.addAttribute("pageTitle", "Product Variant Management");
        return "options/options";
    }
}
