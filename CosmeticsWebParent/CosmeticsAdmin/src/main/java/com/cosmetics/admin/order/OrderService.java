package com.cosmetics.admin.order;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import com.cosmetics.admin.productVariant.ProductVariantRepository;
import com.cosmetics.common.entity.order.OrderDetail;
import com.cosmetics.common.entity.product.ProductVariant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.cosmetics.admin.paging.PagingAndSortingHelper;
import com.cosmetics.admin.setting.country.CountryRepository;
import com.cosmetics.common.entity.Country;
import com.cosmetics.common.entity.order.Order;
import com.cosmetics.common.entity.order.OrderStatus;
import com.cosmetics.common.entity.order.OrderTrack;
import com.cosmetics.common.exception.OrderNotFoundException;

@Service
public class OrderService {
    private static final int ORDERS_PER_PAGE = 10;

    @Autowired
    private OrderRepository orderRepo;
    @Autowired
    private CountryRepository countryRepo;
    @Autowired
    private ProductVariantRepository productVariantRepo;

    public void listByPage(int pageNum, PagingAndSortingHelper helper) {
        String sortField = helper.getSortField();
        String sortDir = helper.getSortDir();
        String keyword = helper.getKeyword();

        Sort sort = null;

        if ("destination".equals(sortField)) {
            sort = Sort.by("country").and(Sort.by("state")).and(Sort.by("city"));
        } else {
            sort = Sort.by(sortField);
        }

        sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
        Pageable pageable = PageRequest.of(pageNum - 1, ORDERS_PER_PAGE, sort);

        Page<Order> page = null;

        if (keyword != null) {
            page = orderRepo.findAll(keyword, pageable);
        } else {
            page = orderRepo.findAll(pageable);
        }

        helper.updateModelAttributes(pageNum, page);
    }

    public Order get(Integer id) throws OrderNotFoundException {
        try {
            return orderRepo.findById(id).get();
        } catch (NoSuchElementException ex) {
            throw new OrderNotFoundException("Could not find any orders with ID " + id);
        }
    }

    public void delete(Integer id) throws OrderNotFoundException {
        Long count = orderRepo.countById(id);
        if (count == null || count == 0) {
            throw new OrderNotFoundException("Could not find any orders with ID " + id);
        }

        orderRepo.deleteById(id);
    }

    public List<Country> listAllCountries() {
        return countryRepo.findAllByOrderByNameAsc();
    }

    public void save(Order orderInForm) {
        Order orderInDB = orderRepo.findById(orderInForm.getId()).get();
        orderInForm.setOrderTime(orderInDB.getOrderTime());
        orderInForm.setCustomer(orderInDB.getCustomer());

        orderRepo.save(orderInForm);
    }

    public void updateStatus(Integer orderId, String status) {
        Order orderInDB = orderRepo.findById(orderId).get();
        OrderStatus statusToUpdate = OrderStatus.valueOf(status);

        if (!orderInDB.hasStatus(statusToUpdate)) {
            List<OrderTrack> orderTracks = orderInDB.getOrderTracks();

            OrderTrack track = new OrderTrack();
            track.setOrder(orderInDB);
            track.setStatus(statusToUpdate);
            track.setUpdatedTime(new Date());
            track.setNotes(statusToUpdate.defaultDescription());

            orderTracks.add(track);

            orderInDB.setStatus(statusToUpdate);

            orderRepo.save(orderInDB);
        }

    }

    public int getExistingOrderQuantity(Integer orderId, Integer productId) {
        return orderRepo.getProductQuantityInOrder(orderId, productId);
    }
    public List<Order> findTop5RecentOrders() {
        return orderRepo.findTop5ByOrderByOrderTimeDesc();
    }

    public void restockFromOrder(Integer orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        for (OrderDetail d : order.getOrderDetails()) {
            ProductVariant v = d.getVariant();
            v.setStock(v.getStock() + d.getQuantity());
            productVariantRepo.save(v);
        }
    }


}
