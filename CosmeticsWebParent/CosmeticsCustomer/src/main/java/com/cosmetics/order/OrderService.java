package com.cosmetics.order;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.cosmetics.common.entity.product.ProductVariant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.cosmetics.checkout.CheckoutInfo;
import com.cosmetics.common.entity.Address;
import com.cosmetics.common.entity.CartItem;
import com.cosmetics.common.entity.Customer;
import com.cosmetics.common.entity.order.Order;
import com.cosmetics.common.entity.order.OrderDetail;
import com.cosmetics.common.entity.order.OrderStatus;
import com.cosmetics.common.entity.order.OrderTrack;
import com.cosmetics.common.entity.order.PaymentMethod;
import com.cosmetics.common.exception.OrderNotFoundException;


@Service
public class OrderService {
    public static final int ORDERS_PER_PAGE = 5;

    @Autowired
    private OrderRepository repo;

    public Order createOrder(Customer customer, Address address, List<CartItem> cartItems,
                             PaymentMethod paymentMethod, CheckoutInfo checkoutInfo) {
        Order newOrder = new Order();
        newOrder.setOrderTime(new Date());

        if (paymentMethod.equals(PaymentMethod.PAYPAL)) {
            newOrder.setStatus(OrderStatus.PAID);
        } else {
            newOrder.setStatus(OrderStatus.NEW);
        }

        newOrder.setCustomer(customer);
        newOrder.setTax(0.0f);
        newOrder.setPaymentMethod(paymentMethod);
        newOrder.setDeliverDays(checkoutInfo.getDeliverDays());

        if (address == null) {
            newOrder.copyAddressFromCustomer();
        } else {
            newOrder.copyShippingAddress(address);
        }

        Set<OrderDetail> orderDetails = newOrder.getOrderDetails();

        for (CartItem cartItem : cartItems) {
            ProductVariant productVariant = cartItem.getVariant();

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(newOrder);
            orderDetail.setVariant(productVariant);
            orderDetail.setQuantity(cartItem.getQuantity());
            orderDetail.setUnitPrice(productVariant.getDiscountPrice());
            orderDetail.setProductCost(productVariant.getCost() * cartItem.getQuantity());
            orderDetail.setShippingCost(cartItem.getShippingCost());

            orderDetails.add(orderDetail);
        }

        List<OrderTrack> tracks = new ArrayList<>();

        OrderTrack newTrack = new OrderTrack();
        newTrack.setOrder(newOrder);
        newTrack.setStatus(OrderStatus.NEW);
        newTrack.setNotes(OrderStatus.NEW.defaultDescription());
        newTrack.setUpdatedTime(new Date());
        tracks.add(newTrack);

        if (paymentMethod.equals(PaymentMethod.PAYPAL)) {
            OrderTrack paidTrack = new OrderTrack();
            paidTrack.setOrder(newOrder);
            paidTrack.setStatus(OrderStatus.PAID);
            paidTrack.setNotes(OrderStatus.PAID.defaultDescription());
            paidTrack.setUpdatedTime(new Date());
            tracks.add(paidTrack);
        }

        newOrder.setOrderTracks(tracks);

        return repo.save(newOrder);
    }

    public Page<Order> listForCustomerByPage(Customer customer, int pageNum,
                                             String sortField, String sortDir, String keyword) {
        Sort sort = Sort.by(sortField);
        sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();

        Pageable pageable = PageRequest.of(pageNum - 1, ORDERS_PER_PAGE, sort);

        if (keyword != null) {
            return repo.findAll(keyword, customer.getId(), pageable);
        }

        return repo.findAll(customer.getId(), pageable);
    }

    public Order getOrder(Integer id, Customer customer) {
        return repo.findByIdAndCustomer(id, customer);
    }

    public void setOrderReturnRequested(OrderReturnRequest request, Customer customer)
            throws OrderNotFoundException {
        Order order = repo.findByIdAndCustomer(request.getOrderId(), customer);
        if (order == null) {
            throw new OrderNotFoundException("Order ID " + request.getOrderId() + " not found");
        }

        if (order.isReturnRequested()) return;

        OrderTrack track = new OrderTrack();
        track.setOrder(order);
        track.setUpdatedTime(new Date());
        track.setStatus(OrderStatus.RETURN_REQUESTED);

        String notes = "Reason: " + request.getReason();
        if (!"".equals(request.getNote())) {
            notes += ". " + request.getNote();
        }

        track.setNotes(notes);

        order.getOrderTracks().add(track);
        order.setStatus(OrderStatus.RETURN_REQUESTED);

        repo.save(order);
    }

}
