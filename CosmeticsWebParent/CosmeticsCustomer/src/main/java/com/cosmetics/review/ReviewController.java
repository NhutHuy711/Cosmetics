package com.cosmetics.review;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.cosmetics.common.entity.product.ProductVariant;
import com.cosmetics.common.exception.ProductVariantNotFoundException;
import com.cosmetics.productVariant.ProductVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cosmetics.ControllerHelper;
import com.cosmetics.category.CategoryService;
import com.cosmetics.common.entity.Category;
import com.cosmetics.common.entity.Customer;
import com.cosmetics.common.entity.Review;
import com.cosmetics.common.exception.ReviewNotFoundException;
import com.cosmetics.product.ProductService;
import com.cosmetics.review.vote.ReviewVoteService;

@Controller
public class ReviewController {
    private String defaultRedirectURL = "redirect:/reviews/page/1?sortField=reviewTime&sortDir=desc";

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ControllerHelper controllerHelper;

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewVoteService voteService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductVariantService productVariantService;

    @GetMapping("/reviews")
    public String listFirstPage(Model model) {
        return defaultRedirectURL;
    }

    @GetMapping("/reviews/page/{pageNum}")
    public String listReviewsByCustomerByPage(Model model, HttpServletRequest request,
                                              @PathVariable(name = "pageNum") int pageNum,
                                              String keyword, String sortField, String sortDir) {
        Customer customer = controllerHelper.getAuthenticatedCustomer(request);
        Page<Review> page = reviewService.listByCustomerByPage(customer, keyword, pageNum, sortField, sortDir);
        List<Review> listReviews = page.getContent();
        List<Category> listCategories = categoryService.listHierarchicalCategories();

        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("keyword", keyword);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("moduleURL", "/reviews");

        model.addAttribute("listReviews", listReviews);
        model.addAttribute("listCategories", listCategories);
        long startCount = (pageNum - 1) * ReviewService.REVIEWS_PER_PAGE + 1;
        model.addAttribute("startCount", startCount);

        long endCount = startCount + ReviewService.REVIEWS_PER_PAGE - 1;
        if (endCount > page.getTotalElements()) {
            endCount = page.getTotalElements();
        }

        model.addAttribute("endCount", endCount);

        return "reviews/reviews_customer";
    }

    @GetMapping("/reviews/detail/{id}")
    public String viewReview(@PathVariable("id") Integer id, Model model,
                             RedirectAttributes ra, HttpServletRequest request) {
        Customer customer = controllerHelper.getAuthenticatedCustomer(request);
        try {
            Review review = reviewService.getByCustomerAndId(customer, id);
            model.addAttribute("review", review);

            return "reviews/review_detail_modal";
        } catch (ReviewNotFoundException ex) {
            ra.addFlashAttribute("message", ex.getMessage());
            return defaultRedirectURL;
        }
    }

    @GetMapping("/ratings/{productAlias}/page/{pageNum}")
    public String listByProductByPage(Model model,
                                      @PathVariable(name = "productAlias") String productAlias,
                                      @PathVariable(name = "pageNum") int pageNum,
                                      String sortField, String sortDir,
                                      HttpServletRequest request) {

        ProductVariant productVariant = null;

        try {
            productVariant = productVariantService.getProductVariant(productAlias);
        } catch (ProductVariantNotFoundException ex) {
            return "error/404";
        }

        Page<Review> page = reviewService.listByProduct(productVariant, pageNum, sortField, sortDir);
        List<Review> listReviews = page.getContent();

        Customer customer = controllerHelper.getAuthenticatedCustomer(request);
        if (customer != null) {
            voteService.markReviewsVotedForProductByCustomer(listReviews, productVariant.getId(), customer.getId());
        }

        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        model.addAttribute("listReviews", listReviews);
        model.addAttribute("product", productVariant);

        long startCount = (pageNum - 1) * ReviewService.REVIEWS_PER_PAGE + 1;
        model.addAttribute("startCount", startCount);

        long endCount = startCount + ReviewService.REVIEWS_PER_PAGE - 1;
        if (endCount > page.getTotalElements()) {
            endCount = page.getTotalElements();
        }

        model.addAttribute("endCount", endCount);
        model.addAttribute("pageTitle", "Reviews for " + productVariant.getName());

        return "reviews/reviews_product";
    }

    @GetMapping("/ratings/{productAlias}")
    public String listByProductFirstPage(@PathVariable(name = "productAlias") String productAlias, Model model,
                                         HttpServletRequest request) {
        return listByProductByPage(model, productAlias, 1, "reviewTime", "desc", request);
    }

    @GetMapping("/write_review/variant/{productId}")
    public String showViewForm(@PathVariable("productId") Integer productVariantId, Model model,
                               HttpServletRequest request) {

        Review review = new Review();

        ProductVariant productVariant = null;

        try {
            productVariant = productVariantService.getProductVariant(productVariantId);
        } catch (ProductVariantNotFoundException ex) {
            return "error/404";
        }

        Customer customer = controllerHelper.getAuthenticatedCustomer(request);
        boolean customerReviewed = reviewService.didCustomerReviewProduct(customer, productVariant.getId());

        if (customerReviewed) {
            model.addAttribute("customerReviewed", customerReviewed);
        } else {
            boolean customerCanReview = reviewService.canCustomerReviewProduct(customer, productVariant.getId());

            if (customerCanReview) {
                model.addAttribute("customerCanReview", customerCanReview);
            } else {
                model.addAttribute("NoReviewPermission", true);
            }
        }

        model.addAttribute("product", productVariant);
        model.addAttribute("review", review);

        return "reviews/review_form";
    }

    @PostMapping("/post_review")
    public String saveReview(Model model, Review review, Integer productId, HttpServletRequest request) {
        Customer customer = controllerHelper.getAuthenticatedCustomer(request);

        ProductVariant productVariant = null;

        try {
            productVariant = productVariantService.getProductVariant(productId);
        } catch (ProductVariantNotFoundException ex) {
            return "error/404";
        }

        review.setProductVariant(productVariant);
        review.setCustomer(customer);

        Review savedReview = reviewService.save(review);

        model.addAttribute("review", savedReview);

        return "reviews/review_done";
    }
}
