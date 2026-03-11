package com.cosmetics.common.entity.order;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.cosmetics.common.entity.AbstractAddress;
import com.cosmetics.common.entity.Address;
import com.cosmetics.common.entity.Customer;
import org.hibernate.annotations.Formula;

@Entity
@Table(name = "orders")
public class Order extends AbstractAddress {

    @Column(nullable = false, length = 45)
    private String country;

    @Column(nullable = false, updatable = false)
    private Date orderTime;

    @Formula("(select COALESCE(sum(od.product_cost),0) from order_details od where od.order_id = id)")
    private float productCost;

    @Formula("(select COALESCE(sum(od.shipping_cost),0) from order_details od where od.order_id = id)")
    private float shippingCost;

    @Formula("(select COALESCE(sum(od.quantity * od.unit_price + od.shipping_cost),0) from order_details od where od.order_id = id)")
    private float subtotal;

    @Column(nullable = false)
    private float tax;

    @Formula("(" +
            " (select COALESCE(sum(od.quantity*od.unit_price + od.shipping_cost),0) " +
            "  from order_details od where od.order_id = id) + tax)")
    private float total;


    @Column(name = "deliver_days", nullable = false)
    private int deliverDays;

    @Formula("ADDTIME({alias}.order_time, SEC_TO_TIME({alias}.deliver_days * 24 * 3600))")
    private Date deliverDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderDetail> orderDetails = new HashSet<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("updatedTime ASC")
    private List<OrderTrack> orderTracks = new ArrayList<>();

    public Order() {
    }

    public Order(Integer id, Date orderTime, float productCost, float subtotal, float shippingCost, float total) {
        this.id = id;
        this.orderTime = orderTime;
        this.productCost = productCost;
        this.subtotal = subtotal;
        this.shippingCost = shippingCost;
        this.total = total;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Date getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public float getShippingCost() {
        return shippingCost;
    }

    public float getProductCost() {
        return productCost;
    }

    public float getSubtotal() {
        return subtotal;
    }

    public float getTax() {
        return tax;
    }

    public void setTax(float tax) {
        this.tax = tax;
    }

    public float getTotal() {
        return total;
    }

    public int getDeliverDays() {
        return deliverDays;
    }

    public void setDeliverDays(int deliverDays) {
        this.deliverDays = deliverDays;
    }

    @Transient
    public Date getDeliverDate() {
        return deliverDate;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Set<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(Set<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    public void copyAddressFromCustomer() {
        setFirstName(customer.getFirstName());
        setLastName(customer.getLastName());
        setPhoneNumber(customer.getPhoneNumber());
        setAddressLine1(customer.getAddressLine1());
        setAddressLine2(customer.getAddressLine2());
        setCity(customer.getCity());
        setCountry(customer.getCountry().getName());
        setPostalCode(customer.getPostalCode());
        setState(customer.getState());
    }

    @Override
    public String toString() {
        return "Order [id=" + id + ", subtotal=" + subtotal + ", paymentMethod=" + paymentMethod + ", status=" + status
                + ", customer=" + customer.getFullName() + "]";
    }

    @Transient
    public String getDestination() {
        return state + ", " + country;
    }

    public void copyShippingAddress(Address address) {
        setFirstName(address.getFirstName());
        setLastName(address.getLastName());
        setPhoneNumber(address.getPhoneNumber());
        setAddressLine1(address.getAddressLine1());
        setAddressLine2(address.getAddressLine2());
        setCity(address.getCity());
        setCountry(address.getCountry().getName());
        setPostalCode(address.getPostalCode());
        setState(address.getState());
    }

    @Transient
    public String getShippingAddress() {
        String address = firstName;

        if (lastName != null && !lastName.isEmpty()) address += " " + lastName;

        if (!addressLine1.isEmpty()) address += ", " + addressLine1;

        if (addressLine2 != null && !addressLine2.isEmpty()) address += ", " + addressLine2;

        if (!city.isEmpty()) address += ", " + city;

        if (state != null && !state.isEmpty()) address += ", " + state;

        address += ", " + country;

        if (!postalCode.isEmpty()) address += ". Postal Code: " + postalCode;
        if (!phoneNumber.isEmpty()) address += ". Phone Number: " + phoneNumber;

        return address;
    }

    public List<OrderTrack> getOrderTracks() {
        return orderTracks;
    }

    public void setOrderTracks(List<OrderTrack> orderTracks) {
        this.orderTracks = orderTracks;
    }

    @Transient
    public String getDeliverDateOnForm() {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormatter.format(this.deliverDate);
    }

    public void setDeliverDateOnForm(String dateString) {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

        try {
            this.deliverDate = dateFormatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Transient
    public String getRecipientName() {
        String name = firstName;
        if (lastName != null && !lastName.isEmpty()) name += " " + lastName;
        return name;
    }

    @Transient
    public String getRecipientAddress() {
        String address = addressLine1;

        if (addressLine2 != null && !addressLine2.isEmpty()) address += ", " + addressLine2;

        if (!city.isEmpty()) address += ", " + city;

        if (state != null && !state.isEmpty()) address += ", " + state;

        address += ", " + country;

        if (!postalCode.isEmpty()) address += ". " + postalCode;

        return address;
    }

    @Transient
    public boolean isCOD() {
        return paymentMethod.equals(PaymentMethod.COD);
    }

    @Transient
    public boolean isProcessing() {
        return hasStatus(OrderStatus.PROCESSING);
    }

    @Transient
    public boolean isPicked() {
        return hasStatus(OrderStatus.PICKED);
    }

    @Transient
    public boolean isShipping() {
        return hasStatus(OrderStatus.SHIPPING);
    }

    @Transient
    public boolean isDelivered() {
        return hasStatus(OrderStatus.DELIVERED);
    }

    @Transient
    public boolean isReturnRequested() {
        return hasStatus(OrderStatus.RETURN_REQUESTED);
    }

    @Transient
    public boolean isReturned() {
        return hasStatus(OrderStatus.RETURNED);
    }

    public boolean hasStatus(OrderStatus status) {
        for (OrderTrack aTrack : orderTracks) {
            if (aTrack.getStatus().equals(status)) {
                if (status.equals(OrderStatus.DELIVERED)) {
                    return isWithin14Days(aTrack);
                }
                return true;
            }
        }
        return false;
    }

    public boolean isWithin14Days(OrderTrack aTrack) {
        Date updatedTime = aTrack.getUpdatedTime(); // Lấy thời gian cập nhật

        // Chuyển đổi Date sang LocalDate
        LocalDate updatedLocalDate = updatedTime.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // Lấy ngày hiện tại
        LocalDate today = LocalDate.now();

        // Tính khoảng cách ngày giữa hai mốc thời gian
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(updatedLocalDate, today);

        // Nếu lớn hơn 14 ngày, trả về false, ngược lại trả về true
        return daysBetween <= 14;
    }

    @Transient
    public String getProductNames() {
        String productNames = "";

        productNames = "<ul>";

        for (OrderDetail detail : orderDetails) {
            String variantName = detail.getVariant() != null ? detail.getVariant().getName() : "";
            productNames += "<li>" + variantName + "</li>";
        }

        productNames += "</ul>";

        return productNames;
    }
}
