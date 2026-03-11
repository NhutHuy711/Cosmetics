package com.cosmetics.admin.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderRestController {

    @Autowired
    private OrderService service;

    @PostMapping("/orders_shipper/update/{id}/{status}")
    public ResponseEntity<OrderStatusResponse> updateOrderStatus(
            @PathVariable("id") Integer orderId,
            @PathVariable("status") String status) {
        service.updateStatus(orderId, status);
        return ResponseEntity.ok(new OrderStatusResponse(orderId, status));
    }

    @PostMapping("/api/orders/{orderId}/restock")
    public ResponseEntity<Void> restock(@PathVariable Integer orderId) {
        service.restockFromOrder(orderId);
        return ResponseEntity.ok().build();
    }

    private static class OrderStatusResponse {
        private final Integer orderId;
        private final String status;

        private OrderStatusResponse(Integer orderId, String status) {
            this.orderId = orderId;
            this.status = status;
        }

        public Integer getOrderId() {
            return orderId;
        }

        public String getStatus() {
            return status;
        }
    }
}
