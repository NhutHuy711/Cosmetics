package com.cosmetics.skinType;

import com.cosmetics.common.entity.SkinResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@Controller
public class SkinTypeController {

    @Autowired
    private SkinTypeService skinTypeService;

    @GetMapping("/skin-analysis")
    public String showPage() {
        return "skin/skin_analysis";  // templates/skin/skin_analysis.html
    }

    @PostMapping("/skin-analysis/submit")
    public String analyzeSkin(@RequestParam("file") MultipartFile file, Model model) throws Exception {
        String[] CLASS_NAMES = {"Combination", "Dry", "Normal", "Oily", "Sensitive"};
        SkinResult result = skinTypeService.analyze(file);
        model.addAttribute("result", result);

        // convert image to Base64
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        model.addAttribute("analyzedImage", base64Image);
        model.addAttribute("classNames", CLASS_NAMES);
        return "skin/skin_analysis";
    }


}
