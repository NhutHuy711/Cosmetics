package com.cosmetics.admin.storage;

import com.cosmetics.admin.MessageServiceAdmin;
import com.cosmetics.admin.paging.PagingAndSortingHelper;
import com.cosmetics.admin.paging.PagingAndSortingParam;
import com.cosmetics.admin.product.ProductService;
import com.cosmetics.admin.productVariant.ProductVariantService;
import com.cosmetics.admin.security.CosmeticsUserDetails;
import com.cosmetics.admin.setting.SettingService;
import com.cosmetics.admin.storage.export.ImportCsvExporter;
import com.cosmetics.admin.storage.export.ImportExcelExporter;
import com.cosmetics.admin.user.UserService;
import com.cosmetics.common.entity.User;
import com.cosmetics.common.entity.product.ProductVariant;
import com.cosmetics.common.entity.setting.Setting;
import com.cosmetics.common.entity.storage.Import;
import com.cosmetics.common.exception.ImportNotFoundException;
import com.cosmetics.common.exception.ProductVariantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
public class ImportController {
    private String defaultRedirectURL = "redirect:/imports/page/1?sortField=transactionTime&sortDir=desc";

    @Autowired
    private ImportService importService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private MessageServiceAdmin messageService;

    @Autowired
    private ProductVariantService productVariantService;

    @GetMapping("/imports")
    public String listFirstPage(Model model) {
        return defaultRedirectURL;
    }

    @GetMapping("/imports/page/{pageNum}")
    public String listByPage(
            @PagingAndSortingParam(listName = "listImports", moduleURL = "/imports") PagingAndSortingHelper helper,
            @PathVariable(name = "pageNum") int pageNum
    ) {
        importService.listByPage(pageNum, helper);
        return "storage/import";
    }
    private void loadCurrencySettingImport(HttpServletRequest request) {
        List<Setting> currencySettings = settingService.getCurrencySettings();

        for (Setting setting : currencySettings) {
            request.setAttribute(setting.getKey(), setting.getValue());
        }
    }

    @GetMapping("/imports/detail/{id}")
    public String viewImportDetails(@PathVariable("id") Integer id, Model model,
            HttpServletRequest request,
            RedirectAttributes ra){
        try{
            Import ip = importService.get(id);
            model.addAttribute("import",ip);
            loadCurrencySettingImport(request);
            return "storage/import_detail_modal";

        }catch (ImportNotFoundException e){
            ra.addFlashAttribute("message", e.getMessage());
            return defaultRedirectURL;
        }
    }

    @GetMapping("/imports/new")
    public String newImport(@AuthenticationPrincipal CosmeticsUserDetails loggedUser, Model model){
        User user = userService.getByEmail(loggedUser.getUsername());
        Import ip = new Import();
        List<ProductVariant> listProductVariants = productVariantService.listAll();

        model.addAttribute("import",ip);
        model.addAttribute("user",user);
        model.addAttribute("listProductVariants",listProductVariants);
        model.addAttribute("pageTitle", "Create New Import");
        model.addAttribute("moduleURL", "/imports/new");

        return "storage/import_form";
    }

    @PostMapping("/imports/save")
    public String saveImport(Import ip,
             @RequestParam(name = "productIds", required = false) String[] detailProductVariantIds,
             @RequestParam(name = "productAmounts", required = false) String[] detailAmounts,
             @RequestParam(name = "productCosts", required = false) String[] detailCosts,
             @AuthenticationPrincipal CosmeticsUserDetails loggedUser,
             RedirectAttributes ra) throws ProductVariantNotFoundException {

        importService.setImportDetails(detailProductVariantIds, detailAmounts, detailCosts, ip);

        String userEmail = loggedUser.getUsername();

        importService.save(ip, userEmail);

        ra.addFlashAttribute("message", messageService.getMessage("IMPORT_SAVE_SUCCESS"));

        return defaultRedirectURL;
    }

    @GetMapping("/imports/export/csv")
    public void exportToCSV(HttpServletResponse response) throws IOException {
        List<Import> listImports = importService.listAll();
        ImportCsvExporter exporter = new ImportCsvExporter();
        exporter.export(listImports, response);
    }

    @GetMapping("/imports/export/excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        List<Import> listImports = importService.listAll();
        ImportExcelExporter exporter = new ImportExcelExporter();
        exporter.export(listImports, response);
    }

}
