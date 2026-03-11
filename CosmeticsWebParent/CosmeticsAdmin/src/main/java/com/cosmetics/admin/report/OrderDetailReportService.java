package com.cosmetics.admin.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cosmetics.admin.order.OrderDetailRepository;
import com.cosmetics.common.entity.order.OrderDetail;

@Service
public class OrderDetailReportService extends AbstractReportService {

    @Autowired
    private OrderDetailRepository repo;

    private boolean isZeroMoneyStatus(String status) {
        if (status == null || status.isBlank()) return false;
        return status.equals("CANCELLED") || status.equals("RETURNED") || status.equals("REFUNDED");
    }


    @Override
    protected List<ReportItem> getReportDataByDateRangeInternal(Date startDate, Date endDate, ReportType reportType) {
        List<OrderDetail> listOrderDetails = null;

        if (reportType.equals(ReportType.CATEGORY)) {
            listOrderDetails = repo.findWithCategoryAndTimeBetween(startDate, endDate);
            // printRawDataCategory(listOrderDetails);
        } else if (reportType.equals(ReportType.PRODUCT)) {
            listOrderDetails = repo.findWithProductAndTimeBetween(startDate, endDate);
        } else {
            listOrderDetails = new ArrayList<>();
        }

        return buildReportItems(listOrderDetails, reportType);
    }


    @Override
    protected List<ReportItem> getReportDataByDateRangeInternal(
            Date startDate, Date endDate, ReportType reportType,
            String status, String payment) {

        boolean zeroMoney = isZeroMoneyStatus(status);

        List<com.cosmetics.common.entity.order.OrderStatus> statuses = null;
        List<com.cosmetics.common.entity.order.PaymentMethod> payments = null;

        if (status != null && !status.isBlank()) {
            try { statuses = List.of(com.cosmetics.common.entity.order.OrderStatus.valueOf(status)); }
            catch (IllegalArgumentException ex) { statuses = null; }
        }
        if (payment != null && !payment.isBlank()) {
            try { payments = List.of(com.cosmetics.common.entity.order.PaymentMethod.valueOf(payment)); }
            catch (IllegalArgumentException ex) { payments = null; }
        }

        List<OrderDetail> listOrderDetails;

        if (reportType.equals(ReportType.CATEGORY)) {
            listOrderDetails = repo.findWithCategoryAndTimeBetweenFiltered(startDate, endDate, statuses, payments);
        } else if (reportType.equals(ReportType.PRODUCT)) {
            listOrderDetails = repo.findWithProductAndTimeBetweenFiltered(startDate, endDate, statuses, payments);
        } else {
            listOrderDetails = new ArrayList<>();
        }

        List<ReportItem> listReportItems = new ArrayList<>();

        for (OrderDetail detail : listOrderDetails) {
            String identifier = reportType.equals(ReportType.CATEGORY)
                    ? detail.getVariant().getProduct().getCategory().getName()
                    : detail.getVariant().getProduct().getShortName();

            ReportItem key = new ReportItem(identifier);
            int idx = listReportItems.indexOf(key);

            if (idx >= 0) {
                ReportItem item = listReportItems.get(idx);
                item.increaseProductsCount(detail.getQuantity());

                if (!zeroMoney) {
                    float revenue = detail.getSubtotal() + detail.getShippingCost();
                    float profit  = detail.getSubtotal() - detail.getProductCost();
                    float shipping = detail.getShippingCost();
                    item.addRevenue(revenue);
                    item.addProfit(profit);
                    item.addShipping(shipping);
                }
            } else {
                int qty = detail.getQuantity();
                if (zeroMoney) {
                    listReportItems.add(new ReportItem(identifier, 0f, 0f, 0f, qty));
                } else {
                    float revenue = detail.getSubtotal() + detail.getShippingCost();
                    float profit  = detail.getSubtotal() - detail.getProductCost();
                    float shipping = detail.getShippingCost();
                    listReportItems.add(new ReportItem(identifier, revenue, profit, shipping, qty));
                }
            }
        }

        return listReportItems;
    }

    private List<ReportItem> buildReportItems(List<OrderDetail> listOrderDetails, ReportType reportType) {
        List<ReportItem> listReportItems = new ArrayList<>();

        for (OrderDetail detail : listOrderDetails) {
            String identifier = "";

            if (reportType.equals(ReportType.CATEGORY)) {
                identifier = detail.getVariant().getProduct().getCategory().getName();
            } else if (reportType.equals(ReportType.PRODUCT)) {
                identifier = detail.getVariant().getProduct().getShortName();
            }

            ReportItem reportItem = new ReportItem(identifier);

            float revenue = detail.getSubtotal() + detail.getShippingCost();
            float profit = detail.getSubtotal() - detail.getProductCost();
            float shipping = detail.getShippingCost();

            int itemIndex = listReportItems.indexOf(reportItem);

            if (itemIndex >= 0) {
                reportItem = listReportItems.get(itemIndex);
                reportItem.addRevenue(revenue);
                reportItem.addProfit(profit);
                reportItem.addShipping(shipping);
                reportItem.increaseProductsCount(detail.getQuantity());
            } else {
                listReportItems.add(new ReportItem(identifier, revenue, profit, shipping, detail.getQuantity()));
            }
        }

        // printReportData(listReportItems);
        return listReportItems;
    }



    private void printReportData(List<ReportItem> listReportItems) {
        for (ReportItem item : listReportItems) {
            System.out.printf("%-20s, %10.2f, %10.2f, %10.2f, %d \n",
                    item.getIdentifier(), item.getRevenue(), item.getProfit(), item.getShippingCost(), item.getProductsCount());
        }
    }

    private void printRawDataCategory(List<OrderDetail> listOrderDetails) {
        for (OrderDetail detail : listOrderDetails) {
            System.out.printf("%d, %-20s, %10.2f, %10.2f, %10.2f \n",
                    detail.getQuantity(), detail.getVariant().getProduct().getCategory().getName(),
                    detail.getSubtotal(), detail.getProductCost(), detail.getShippingCost());
        }
    }

}
