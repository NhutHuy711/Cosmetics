// Sales Report by Category
var data;
var chartOptions;

$(document).ready(function() {
	setupButtonEventHandlers("_category", loadSalesReportByDateForCategory);
});

function loadSalesReportByDateForCategory(period) {
    const filterParams = buildFilterQueryParams("_category");

    if (period == "custom") {
        startDate = $("#startDate_category").val();
        endDate = $("#endDate_category").val();
        requestURL = contextPath + "reports/category/" + startDate + "/" + endDate + filterParams;
    } else {
        requestURL = contextPath + "reports/category/" + period + filterParams;
    }

    $.get(requestURL, function(responseJSON) {
        prepareChartDataForSalesReportByCategory(responseJSON);
        customizeChartForSalesReportByCategory();
        const status = getSelectedStatus("_category");
        if (!isZeroMoneyStatusJS(status)) {
            formatChartData(data, 1, 2); // revenue & profit
        }
        drawChartForSalesReportByCategory(period);
        setSalesAmount(period, '_category', "Total Products");
    });
}

function getSelectedStatus(reportType) {
    return ($("#statusFilter" + reportType).val() || "").toUpperCase();
}

function isZeroMoneyStatusJS(status) {
    return status === "CANCELLED" || status === "REFUNDED";
}


function prepareChartDataForSalesReportByCategory(responseJSON) {
    const status = getSelectedStatus("_category");
    const useQuantity = isZeroMoneyStatusJS(status);

    data = new google.visualization.DataTable();
    data.addColumn('string', 'Category');

    if (useQuantity) {
        data.addColumn('number', 'Quantity');
    } else {
        data.addColumn('number', 'Revenue');
        data.addColumn('number', 'Profit');
        data.addColumn('number', 'Shipping Cost');
    }

    totalRevenue = 0.0;
    totalProfit = 0.0;
    totalShippingCost = 0.0;
    totalItems = 0;

    $.each(responseJSON, function(index, reportItem) {
        const qty = parseInt(reportItem.productsCount) || 0;

        if (useQuantity) {
            data.addRows([[reportItem.identifier, qty]]);
        } else {
            data.addRows([[reportItem.identifier, reportItem.revenue, reportItem.profit, reportItem.shippingCost]]);
            totalRevenue += parseFloat(reportItem.revenue) || 0;
            totalProfit += parseFloat(reportItem.profit) || 0;
            totalShippingCost += parseFloat(reportItem.shippingCost) || 0;
        }

        totalItems += qty;
    });
}


function customizeChartForSalesReportByCategory() {
    const status = getSelectedStatus("_category");
    const useQuantity = isZeroMoneyStatusJS(status);

    chartOptions = {
        height: 360,
        legend: { position: 'right' },
        title: useQuantity ? "Products Quantity by Category" : "Revenue by Category"
    };
}


function drawChartForSalesReportByCategory() {
	var salesChart = new google.visualization.PieChart(document.getElementById('chart_sales_by_category'));
	salesChart.draw(data, chartOptions);
}