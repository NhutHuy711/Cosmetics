package com.cosmetics.checkout;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import com.cosmetics.common.entity.product.ProductVariant;
import com.cosmetics.common.exception.ProductVariantNotFoundException;
import com.cosmetics.product.ProductService;
import com.cosmetics.productVariant.ProductVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import com.cosmetics.ControllerHelper;
import com.cosmetics.Utility;
import com.cosmetics.address.AddressService;
import com.cosmetics.category.CategoryService;
import com.cosmetics.checkout.paypal.PayPalApiException;
import com.cosmetics.checkout.paypal.PayPalService;
import com.cosmetics.common.entity.Address;
import com.cosmetics.common.entity.CartItem;
import com.cosmetics.common.entity.Category;
import com.cosmetics.common.entity.Customer;
import com.cosmetics.common.entity.ShippingRate;
import com.cosmetics.common.entity.order.Order;
import com.cosmetics.common.entity.order.PaymentMethod;
import com.cosmetics.order.OrderService;
import com.cosmetics.setting.CurrencySettingBag;
import com.cosmetics.setting.EmailSettingBag;
import com.cosmetics.setting.PaymentSettingBag;
import com.cosmetics.setting.SettingService;
import com.cosmetics.shipping.ShippingRateService;
import com.cosmetics.shoppingcart.ShoppingCartService;

@Controller
public class CheckoutController {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private ControllerHelper controllerHelper;

    @Autowired
    private AddressService addressService;

    @Autowired
    private ShippingRateService shipService;

    @Autowired
    private ShoppingCartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private PayPalService paypalService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductVariantService productVariantService;

    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, HttpServletRequest request) {
        Customer customer = controllerHelper.getAuthenticatedCustomer(request);

        Address defaultAddress = addressService.getDefaultAddress(customer);
        ShippingRate shippingRate = null;

        if (defaultAddress != null) {
            model.addAttribute("shippingAddress", defaultAddress.toString());
            shippingRate = shipService.getShippingRateForAddress(defaultAddress);
        } else {
            model.addAttribute("shippingAddress", customer.toString());
            shippingRate = shipService.getShippingRateForCustomer(customer);
        }

        if (shippingRate == null) {
            return "redirect:/cart";
        }

        List<CartItem> cartItems = cartService.listCartItems(customer);
        CheckoutInfo checkoutInfo = checkoutService.prepareCheckout(cartItems, shippingRate);
        List<Category> listCategories = categoryService.listHierarchicalCategories();

        String currencyCode = settingService.getCurrencyCode();
        PaymentSettingBag paymentSettings = settingService.getPaymentSettings();
        String paypalClientId = paymentSettings.getClientID();

        model.addAttribute("paypalClientId", paypalClientId);
        model.addAttribute("currencyCode", currencyCode);
        model.addAttribute("customer", customer);
        model.addAttribute("checkoutInfo", checkoutInfo);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("listCategories", listCategories);
        return "checkout/checkout";
    }

    @PostMapping("/place_order")
    public String placeOrder(Model model, HttpServletRequest request, RedirectAttributes ra)
            throws UnsupportedEncodingException, MessagingException, ProductVariantNotFoundException {
        String paymentType = request.getParameter("paymentMethod");
        PaymentMethod paymentMethod = PaymentMethod.valueOf(paymentType);

        Customer customer = controllerHelper.getAuthenticatedCustomer(request);

        Address defaultAddress = addressService.getDefaultAddress(customer);
        ShippingRate shippingRate = null;

        if (defaultAddress != null) {
            shippingRate = shipService.getShippingRateForAddress(defaultAddress);
        } else {
            shippingRate = shipService.getShippingRateForCustomer(customer);
        }

        List<CartItem> cartItems = cartService.listCartItems(customer);

        // --- Bước kiểm tra tồn kho ---
        List<String> oosItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            ProductVariant fresh = productVariantService.getProductVariant(item.getVariant().getId());
            int available = fresh.getStock();

            if (!fresh.isEnabled() || available <= 0 || item.getQuantity() > available) {
                String name = fresh.getName();
                oosItems.add(String.format("%s (only %d left)", name, Math.max(available, 0)));
            }
        }

        if (!oosItems.isEmpty()) {
            ra.addFlashAttribute("toastType", "error");
            ra.addFlashAttribute("toastMsg",
                    "Some items are out of stock or insufficient: " + String.join(", ", oosItems));
            return "redirect:/";
        }

        CheckoutInfo checkoutInfo = checkoutService.prepareCheckout(cartItems, shippingRate);

        Order createdOrder = orderService.createOrder(customer, defaultAddress, cartItems, paymentMethod, checkoutInfo);
        cartService.deleteByCustomer(customer);
        sendOrderConfirmationEmail(request, createdOrder);

        model.addAttribute("order", createdOrder);
        return "checkout/order_completed";
    }

    private void sendOrderConfirmationEmail(HttpServletRequest request, Order order)
            throws UnsupportedEncodingException, MessagingException {
        EmailSettingBag emailSettings = settingService.getEmailSettings();
        JavaMailSenderImpl mailSender = Utility.prepareMailSender(emailSettings);
        mailSender.setDefaultEncoding("utf-8");

        String toAddress = order.getCustomer().getEmail();
        String subject = emailSettings.getOrderConfirmationSubject();
        String content = emailSettings.getOrderConfirmationContent();

        subject = subject.replace("[[orderId]]", String.valueOf(order.getId()));

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(emailSettings.getFromAddress(), emailSettings.getSenderName());
        helper.setTo(toAddress);
        helper.setSubject(subject);

        DateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss E, dd MMM yyyy");
        String orderTime = dateFormatter.format(order.getOrderTime());

        CurrencySettingBag currencySettings = settingService.getCurrencySettings();
        String totalAmount = Utility.formatCurrency(order.getTotal(), currencySettings);

        content = content.replace("[[name]]", order.getCustomer().getFullName());
        content = content.replace("[[orderId]]", String.valueOf(order.getId()));
        content = content.replace("[[orderTime]]", orderTime);
        content = content.replace("[[shippingAddress]]", order.getShippingAddress());
        content = content.replace("[[total]]", totalAmount);
        content = content.replace("[[paymentMethod]]", order.getPaymentMethod().toString());

        helper.setText(content, true);
        mailSender.send(message);
    }

    @PostMapping("/process_paypal_order")
    public String processPayPalOrder(HttpServletRequest request, Model model, RedirectAttributes ra)
            throws UnsupportedEncodingException, MessagingException {
        String orderId = request.getParameter("orderId");
        String pageTitle = "Checkout Failure";

        try {
            // BẮT BUỘC: capture trước khi tạo đơn
            boolean paid = paypalService.captureAndVerify(orderId);
            if (!paid) {
                model.addAttribute("pageTitle", pageTitle);
                model.addAttribute("title", pageTitle);
                model.addAttribute("message", "ERROR: Payment not completed (status != COMPLETED).");
                return "message";
            }

            // Đảm bảo placeOrder nhận được paymentMethod=PAYPAL
            javax.servlet.http.HttpServletRequestWrapper wrapper = new javax.servlet.http.HttpServletRequestWrapper(request) {
                @Override public String getParameter(String name) {
                    if ("paymentMethod".equals(name)) return PaymentMethod.PAYPAL.name();
                    return super.getParameter(name);
                }
            };
            return placeOrder(model, wrapper, ra);

        } catch (PayPalApiException | ProductVariantNotFoundException e) {
            model.addAttribute("pageTitle", pageTitle);
            model.addAttribute("title", pageTitle);
            model.addAttribute("message", "ERROR: Transaction failed: " + e.getMessage());
            return "message";
        }
    }


}
