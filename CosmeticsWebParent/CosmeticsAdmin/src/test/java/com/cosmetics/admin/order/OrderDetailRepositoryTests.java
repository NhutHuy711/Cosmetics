//package com.cosmetics.admin.order;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import java.math.BigDecimal;
//import java.text.DateFormat;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.test.annotation.Rollback;
//
//import com.cosmetics.common.entity.order.OrderDetail;
//
//@DataJpaTest
//@AutoConfigureTestDatabase(replace = Replace.NONE)
//@Rollback(false)
//public class OrderDetailRepositoryTests {
//
//    @Autowired
//    private OrderDetailRepository repo;
//
//    @Test
//    public void testFindWithCategoryAndTimeBetween() throws ParseException {
//        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
//        Date startTime = dateFormatter.parse("2024-11-15");
//        Date endTime = dateFormatter.parse("2024-11-19");
//
//        List<OrderDetail> listOrderDetails = repo.findWithCategoryAndTimeBetween(startTime, endTime);
//
//        assertThat(listOrderDetails.size()).isGreaterThan(0);
//
//        BigDecimal totalCost = BigDecimal.ZERO;
//        BigDecimal totalShipping = BigDecimal.ZERO;
//        BigDecimal total = BigDecimal.ZERO;
//        for (OrderDetail detail : listOrderDetails) {
//            System.out.printf("%-30s | %d | %10.2f| %10.2f | %10.2f \n",
//                    detail.getVariant().getProduct().getCategory().getName(),
//                    detail.getQuantity(), detail.getProductCost().doubleValue(),
//                    detail.getShippingCost().doubleValue(), detail.getSubtotal());
//            totalCost = totalCost.add(detail.getProductCost());
//            total = total.add(BigDecimal.valueOf(detail.getSubtotal()));
//            totalShipping = totalShipping.add(detail.getShippingCost());
//        }
////        System.out.println(totalCost);
//        System.out.println(totalShipping);
//        System.out.println(total);
//    }
//
//    @Test
//    public void testFindWithProductAndTimeBetween() throws ParseException {
//        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
//        Date startTime = dateFormatter.parse("2024-11-15");
//        Date endTime = dateFormatter.parse("2024-11-19");
//
//        List<OrderDetail> listOrderDetails = repo.findWithProductAndTimeBetween(startTime, endTime);
//
//        assertThat(listOrderDetails.size()).isGreaterThan(0);
//
//        BigDecimal totalCost = BigDecimal.ZERO;
//        BigDecimal totalShipping = BigDecimal.ZERO;
//        BigDecimal total = BigDecimal.ZERO;
//        for (OrderDetail detail : listOrderDetails) {
//            System.out.printf("%-70s | %d | %10.2f| %10.2f | %10.2f \n",
//                    detail.getVariant().getProduct().getShortName(),
//                    detail.getQuantity(), detail.getProductCost().doubleValue(),
//                    detail.getShippingCost().doubleValue(), detail.getSubtotal());
//            totalCost = totalCost.add(detail.getProductCost());
//            total = total.add(BigDecimal.valueOf(detail.getSubtotal()));
//            totalShipping = totalShipping.add(detail.getShippingCost());
//        }
//        System.out.println(totalCost);
//        System.out.println(totalShipping);
//        System.out.println(total);
//    }
//}
