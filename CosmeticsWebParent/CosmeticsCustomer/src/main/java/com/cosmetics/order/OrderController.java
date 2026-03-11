package com.cosmetics.order;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.cosmetics.common.entity.product.ProductVariant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cosmetics.ControllerHelper;
import com.cosmetics.category.CategoryService;
import com.cosmetics.common.entity.Category;
import com.cosmetics.common.entity.Customer;
import com.cosmetics.common.entity.order.Order;
import com.cosmetics.common.entity.order.OrderDetail;
import com.cosmetics.review.ReviewService;

@Controller
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private ControllerHelper controllerHelper;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/orders")
    public String listFirstPage(Model model, HttpServletRequest request) {
        return listOrdersByPage(model, request, 1, "orderTime", "desc", null);
    }

    @GetMapping("/orders/page/{pageNum}")
    public String listOrdersByPage(Model model, HttpServletRequest request,
                                   @PathVariable(name = "pageNum") int pageNum,
                                   String sortField, String sortDir, String keyword
    ) {
        Customer customer = controllerHelper.getAuthenticatedCustomer(request);

        Page<Order> page = orderService.listForCustomerByPage(customer, pageNum, sortField, sortDir, keyword);
        List<Order> listOrders = page.getContent();
        List<Category> listCategories = categoryService.listHierarchicalCategories();
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("listOrders", listOrders);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("keyword", keyword);
        model.addAttribute("moduleURL", "/orders");
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("listCategories", listCategories);
        long startCount = (pageNum - 1) * OrderService.ORDERS_PER_PAGE + 1;
        model.addAttribute("startCount", startCount);

        long endCount = startCount + OrderService.ORDERS_PER_PAGE - 1;
        if (endCount > page.getTotalElements()) {
            endCount = page.getTotalElements();
        }

        model.addAttribute("endCount", endCount);

        return "orders/orders_customer";
    }

    @GetMapping("/orders/detail/{id}")
    public String viewOrderDetails(Model model,
                                   @PathVariable(name = "id") Integer id, HttpServletRequest request) {
        Customer customer = controllerHelper.getAuthenticatedCustomer(request);
        Order order = orderService.getOrder(id, customer);

        setProductReviewableStatus(customer, order);
        model.addAttribute("order", order);

        return "orders/order_details_modal";
    }

    private void setProductReviewableStatus(Customer customer, Order order) {
        System.out.println("I am here");
        for (OrderDetail orderDetail : order.getOrderDetails()) {
            ProductVariant productVariant = orderDetail.getVariant();
            Integer productVariantId = productVariant.getId();

            boolean didCustomerReviewProduct = reviewService.didCustomerReviewProduct(customer, productVariantId);
            productVariant.setReviewedByCustomer(didCustomerReviewProduct);
            System.out.println("didCustomerReviewProduct " + didCustomerReviewProduct);
            if (!didCustomerReviewProduct) {
                boolean canCustomerReviewProduct = reviewService.canCustomerReviewProduct(customer, productVariantId);
                System.out.println("canCustomerReviewProduct " + canCustomerReviewProduct);
                productVariant.setCustomerCanReview(canCustomerReviewProduct);
            }

        }
    }
}
