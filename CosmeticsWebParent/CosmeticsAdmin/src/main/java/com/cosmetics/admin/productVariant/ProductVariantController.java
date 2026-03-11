package com.cosmetics.admin.productVariant;

import com.cosmetics.admin.MessageServiceAdmin;
import com.cosmetics.admin.brand.BrandService;
import com.cosmetics.admin.category.CategoryService;
import com.cosmetics.admin.option.OptionService;
import com.cosmetics.admin.paging.PagingAndSortingHelper;
import com.cosmetics.admin.paging.PagingAndSortingParam;
import com.cosmetics.admin.product.ProductService;
import com.cosmetics.admin.security.CosmeticsUserDetails;
import com.cosmetics.common.entity.Brand;
import com.cosmetics.common.entity.Category;
import com.cosmetics.common.entity.product.OptionType;
import com.cosmetics.common.entity.product.Product;
import com.cosmetics.common.entity.product.ProductVariant;
import com.cosmetics.common.exception.ProductVariantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
public class ProductVariantController {
    private String defaultRedirectURL = "redirect:/productVariants/page/1?sortField=id&sortDir=asc&categoryId=0";

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductVariantService productVariantService;

    @Autowired
    private OptionService optionService;

    @Autowired
    private MessageServiceAdmin messageService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @GetMapping("/productVariants/new")
    public String newProduct(Model model) {
        List<Product> listProducts = productService.listAll();
        List<OptionType> listOptionTypes = optionService.listAll();


        ProductVariant productVariant = new ProductVariant();
        productVariant.setEnabled(true);

        model.addAttribute("productVariant", productVariant);
        model.addAttribute("listProducts", listProducts);
        model.addAttribute("listOptionTypes", listOptionTypes);
        model.addAttribute("pageTitle", "Add New Product Variant");
        model.addAttribute("numberOfExistingExtraImages", 0);
        model.addAttribute("moduleURL", "/productVariants/new");

        return "productVariants/productVariant_form";
    }

    @PostMapping("/productVariants/save")
    public String saveProduct(ProductVariant productVariant, RedirectAttributes ra,
                              @RequestParam(value = "fileImage", required = false) MultipartFile mainImageMultipart,
                              @RequestParam(value = "extraImage", required = false) MultipartFile[] extraImageMultiparts,
//                              @RequestParam(name = "detailIDs", required = false) String[] detailIDs,
//                              @RequestParam(name = "detailNames", required = false) String[] detailNames,
//                              @RequestParam(name = "detailValues", required = false) String[] detailValues,
                              @RequestParam(name = "imageIDs", required = false) String[] imageIDs,
                              @RequestParam(name = "imageNames", required = false) String[] imageNames,
                              @RequestParam(name = "optionValueIds", required = false) Integer[] optionValues,
                              @AuthenticationPrincipal CosmeticsUserDetails loggedUser
    ) throws IOException {

        if (!loggedUser.hasRole("Admin") && !loggedUser.hasRole("Editor")) {
            if (loggedUser.hasRole("Salesperson")) {
                productVariantService.saveProductPrice(productVariant);
                ra.addFlashAttribute("message", "The product variant has been saved successfully.");
                return defaultRedirectURL;
            }
        }

        ProductVariantSaveHelper.setMainImageName(mainImageMultipart, productVariant);
        ProductVariantSaveHelper.setExistingExtraImageNames(imageIDs, imageNames, productVariant);
        ProductVariantSaveHelper.setNewExtraImageNames(extraImageMultiparts, productVariant);
//        ProductSaveHelper.setProductDetails(detailIDs, detailNames, detailValues, product);

        ProductVariant savedProductVariant = productVariantService.save(productVariant, optionValues);
//
        ProductVariantSaveHelper.saveUploadedImages(mainImageMultipart, extraImageMultiparts, savedProductVariant);
//
        ProductVariantSaveHelper.deleteExtraImagesWeredRemovedOnForm(productVariant);

        ra.addFlashAttribute("message", messageService.getMessage("SUCCESS_PRODUCT"));
//        ra.addFlashAttribute("message", "The product has been saved successfully.");

        return defaultRedirectURL;
    }

    @GetMapping("/productVariants")
    public String listFirstPage(Model model) {
        return defaultRedirectURL;
    }

    @GetMapping("/productVariants/page/{pageNum}")
    public String listByPage(
            @PagingAndSortingParam(listName = "listProductVariants", moduleURL = "/productVariants") PagingAndSortingHelper helper,
            @PathVariable(name = "pageNum") int pageNum, Model model,
            Integer categoryId
    ) {

        productVariantService.listByPage(pageNum, helper, categoryId);

        List<Category> listCategories = categoryService.listCategoriesUsedInForm();

        if (categoryId != null) model.addAttribute("categoryId", categoryId);
        model.addAttribute("listCategories", listCategories);

        return "productVariants/productVariants";
    }

    @GetMapping("/productVariants/{id}/enabled/{status}")
    public String updateProductEnabledStatus(@PathVariable("id") Integer id,
                                             @PathVariable("status") boolean enabled, RedirectAttributes redirectAttributes) {
        productVariantService.updateProductEnabledStatus(id, enabled);
        String status = enabled ? "enabled" : "disabled";
        String message = "The Product Variant ID " + id + " has been " + status;
        redirectAttributes.addFlashAttribute("message", message);

        return defaultRedirectURL;
    }

    @GetMapping("/productVariants/detail/{id}")
    public String viewProductDetails(@PathVariable("id") Integer id, Model model,
                                     RedirectAttributes ra) {
        try {
            ProductVariant productVariant = productVariantService.get(id);
            model.addAttribute("productVariant", productVariant);

            return "productVariants/productVariant_detail_modal";

        } catch (ProductVariantNotFoundException e) {
            ra.addFlashAttribute("message", e.getMessage());

            return defaultRedirectURL;
        }
    }

    @GetMapping("/productVariants/edit/{id}")
    public String editProduct(@PathVariable("id") Integer id, Model model,
                              RedirectAttributes ra, @AuthenticationPrincipal CosmeticsUserDetails loggedUser) {
        try {
            ProductVariant productVariant = productVariantService.get(id);
            List<Brand> listBrands = brandService.listAll();
            List<Product> listProducts = productService.listAll();
            List<OptionType> listOptionTypes = optionService.listAllWithValue();
            Integer numberOfExistingExtraImages = productVariant.getImages().size();

            boolean isReadOnlyForSalesperson = false;

            if (!loggedUser.hasRole("Admin") && !loggedUser.hasRole("Editor")) {
                if (loggedUser.hasRole("Salesperson")) {
                    isReadOnlyForSalesperson = true;
                }
            }

            model.addAttribute("isReadOnlyForSalesperson", isReadOnlyForSalesperson);
            model.addAttribute("productVariant", productVariant);
            model.addAttribute("listBrands", listBrands);
            model.addAttribute("listProducts", listProducts);
            model.addAttribute("listOptionTypes", listOptionTypes);
            model.addAttribute("pageTitle", "Edit Product (ID: " + id + ")");
            model.addAttribute("numberOfExistingExtraImages", numberOfExistingExtraImages);

            return "productVariants/productVariant_form";

        } catch (ProductVariantNotFoundException e) {
            ra.addFlashAttribute("message", e.getMessage());

            return defaultRedirectURL;
        }
    }


}
