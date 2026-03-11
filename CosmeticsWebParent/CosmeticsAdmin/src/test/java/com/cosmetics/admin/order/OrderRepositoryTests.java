package com.cosmetics.admin.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;

import com.cosmetics.common.entity.Customer;
import com.cosmetics.common.entity.order.Order;
import com.cosmetics.common.entity.order.OrderDetail;
import com.cosmetics.common.entity.order.OrderStatus;
import com.cosmetics.common.entity.order.OrderTrack;
import com.cosmetics.common.entity.order.PaymentMethod;
import com.cosmetics.common.entity.product.Product;
import com.cosmetics.common.entity.product.ProductVariant;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class OrderRepositoryTests {

    @Autowired
    private OrderRepository repo;
    @Autowired
    private TestEntityManager entityManager;

//    @Test
//    public void testCreateNewOrderWithSingleProduct() {
//        Customer customer = entityManager.find(Customer.class, 1);
//        Product product = entityManager.find(Product.class, 1);
//        ProductVariant variant = getRequiredVariant(product);
//
//        Order mainOrder = new Order();
//        mainOrder.setOrderTime(new Date());
//        mainOrder.setCustomer(customer);
//        mainOrder.copyAddressFromCustomer();
//
//        mainOrder.setShippingCost(10);
//        mainOrder.setProductCost(variant.getCost().floatValue());
//        mainOrder.setTax(0);
//        mainOrder.setSubtotal(variant.getPrice().floatValue());
//        mainOrder.setTotal(variant.getPrice().floatValue() + 10);
//
//        mainOrder.setPaymentMethod(PaymentMethod.CREDIT_CARD);
//        mainOrder.setStatus(OrderStatus.NEW);
//        mainOrder.setDeliverDate(new Date());
//        mainOrder.setDeliverDays(1);
//
//        OrderDetail orderDetail = new OrderDetail();
//        orderDetail.setVariant(variant);
//        orderDetail.setOrder(mainOrder);
//        orderDetail.setProductCost(variant.getCost());
//        orderDetail.setShippingCost(BigDecimal.valueOf(10));
//        orderDetail.setQuantity(1);
//        orderDetail.setSubtotal(variant.getPrice().floatValue());
//        orderDetail.setUnitPrice(variant.getPrice());
//
//        mainOrder.getOrderDetails().add(orderDetail);
//
//        Order savedOrder = repo.save(mainOrder);
//
//        assertThat(savedOrder.getId()).isGreaterThan(0);
//    }

//    @Test
//    public void testCreateNewOrderWithMultipleProducts() {
//        Customer customer = entityManager.find(Customer.class, 10);
//        Product product1 = entityManager.find(Product.class, 20);
//        Product product2 = entityManager.find(Product.class, 40);
//        ProductVariant variant1 = getRequiredVariant(product1);
//        ProductVariant variant2 = getRequiredVariant(product2);
//
//        Order mainOrder = new Order();
//        mainOrder.setOrderTime(new Date());
//        mainOrder.setCustomer(customer);
//        mainOrder.copyAddressFromCustomer();
//
//        OrderDetail orderDetail1 = new OrderDetail();
//        orderDetail1.setVariant(variant1);
//        orderDetail1.setOrder(mainOrder);
//        orderDetail1.setProductCost(variant1.getCost());
//        orderDetail1.setShippingCost(BigDecimal.valueOf(10));
//        orderDetail1.setQuantity(1);
//        orderDetail1.setSubtotal(variant1.getPrice().floatValue());
//        orderDetail1.setUnitPrice(variant1.getPrice());
//
//        OrderDetail orderDetail2 = new OrderDetail();
//        orderDetail2.setVariant(variant2);
//        orderDetail2.setOrder(mainOrder);
//        orderDetail2.setProductCost(variant2.getCost());
//        orderDetail2.setShippingCost(BigDecimal.valueOf(20));
//        orderDetail2.setQuantity(2);
//        orderDetail2.setSubtotal(variant2.getPrice().multiply(BigDecimal.valueOf(2)).floatValue());
//        orderDetail2.setUnitPrice(variant2.getPrice());
//
//        mainOrder.getOrderDetails().add(orderDetail1);
//        mainOrder.getOrderDetails().add(orderDetail2);
//
//        mainOrder.setShippingCost(30);
//        mainOrder.setProductCost(variant1.getCost().floatValue() + variant2.getCost().floatValue());
//        mainOrder.setTax(0);
//        float subtotal = variant1.getPrice().floatValue() + variant2.getPrice().floatValue() * 2;
//        mainOrder.setSubtotal(subtotal);
//        mainOrder.setTotal(subtotal + 30);
//
//        mainOrder.setPaymentMethod(PaymentMethod.CREDIT_CARD);
//        mainOrder.setStatus(OrderStatus.PACKAGED);
//        mainOrder.setDeliverDate(new Date());
//        mainOrder.setDeliverDays(3);
//
//        Order savedOrder = repo.save(mainOrder);
//        assertThat(savedOrder.getId()).isGreaterThan(0);
//    }

    @Test
    public void testListOrders() {
        Iterable<Order> orders = repo.findAll();

        assertThat(orders).hasSizeGreaterThan(0);

        orders.forEach(System.out::println);
    }

    @Test
    public void testUpdateOrder() {
        Integer orderId = 2;
        Order order = repo.findById(orderId).get();

        order.setStatus(OrderStatus.SHIPPING);
        order.setPaymentMethod(PaymentMethod.COD);
        order.setOrderTime(new Date());
        order.setDeliverDays(2);

        Order updatedOrder = repo.save(order);

        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.SHIPPING);
    }

    @Test
    public void testGetOrder() {
        Integer orderId = 3;
        Order order = repo.findById(orderId).get();

        assertThat(order).isNotNull();
        System.out.println(order);
    }

    @Test
    public void testDeleteOrder() {
        Integer orderId = 3;
        repo.deleteById(orderId);

        Optional<Order> result = repo.findById(orderId);
        assertThat(result).isNotPresent();
    }

    @Test
    public void testUpdateOrderTracks() {
        Integer orderId = 19;
        Order order = repo.findById(orderId).get();

        OrderTrack newTrack = new OrderTrack();
        newTrack.setOrder(order);
        newTrack.setUpdatedTime(new Date());
        newTrack.setStatus(OrderStatus.NEW);
        newTrack.setNotes(OrderStatus.NEW.defaultDescription());

        OrderTrack processingTrack = new OrderTrack();
        processingTrack.setOrder(order);
        processingTrack.setUpdatedTime(new Date());
        processingTrack.setStatus(OrderStatus.PROCESSING);
        processingTrack.setNotes(OrderStatus.PROCESSING.defaultDescription());

        List<OrderTrack> orderTracks = order.getOrderTracks();
        orderTracks.add(newTrack);
        orderTracks.add(processingTrack);

        Order updatedOrder = repo.save(order);

        assertThat(updatedOrder.getOrderTracks()).hasSizeGreaterThan(1);
    }

    @Test
    public void testAddTrackWithStatusNewToOrder() {
        Integer orderId = 2;
        Order order = repo.findById(orderId).get();

        OrderTrack newTrack = new OrderTrack();
        newTrack.setOrder(order);
        newTrack.setUpdatedTime(new Date());
        newTrack.setStatus(OrderStatus.NEW);
        newTrack.setNotes(OrderStatus.NEW.defaultDescription());

        List<OrderTrack> orderTracks = order.getOrderTracks();
        orderTracks.add(newTrack);

        Order updatedOrder = repo.save(order);

        assertThat(updatedOrder.getOrderTracks()).hasSizeGreaterThan(1);
    }

    @Test
    public void testFindByOrderTimeBetween() throws ParseException {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date startTime = dateFormatter.parse("2024-11-15");
        Date endTime = dateFormatter.parse("2024-11-19");

        List<Order> listOrders = repo.findByOrderTimeBetween(startTime, endTime);

        assertThat(listOrders.size()).isGreaterThan(0);

        float totalCost = 0;
        float totalShipping = 0;
        float total = 0;
        for (Order order : listOrders) {
            System.out.printf("%s | %s | %.2f | %.2f | %.2f \n",
                    order.getId(), order.getOrderTime(), order.getProductCost(),
                    order.getSubtotal(), order.getTotal());
            totalCost += order.getProductCost();
            total += order.getSubtotal();
            totalShipping += order.getShippingCost();
        }
        System.out.println(totalCost);
        System.out.println(totalShipping);
        System.out.println(total);
    }

    private ProductVariant getRequiredVariant(Product product) {
        return product.getVariants()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Product has no variants"));
    }
}
